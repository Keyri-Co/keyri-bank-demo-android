package com.keyrico.keyrisdk.services

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.keyrico.keyrisdk.Keyri.Companion.ANON_USER
import com.keyrico.keyrisdk.exception.NoAssociationKeyPresentException
import com.keyrico.keyrisdk.services.backup.BackupCallbacks
import com.keyrico.keyrisdk.services.backup.BackupService
import com.keyrico.keyrisdk.telemetry.TelemetryCodes
import com.keyrico.keyrisdk.telemetry.TelemetryManager
import com.keyrico.keyrisdk.utils.toByteArrayFromBase64String
import com.keyrico.keyrisdk.utils.toStringBase64
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class CryptoService(
    private val context: Context,
    appKey: String,
    blockSwizzleDetection: Boolean,
) : BackupCallbacks {
    private val backupService = BackupService(context, this, blockSwizzleDetection)

    init {
        createAnonymousECDSAKeypair()
        backupService.checkBackupAccounts()
    }

    override suspend fun onGetAssociationKey(publicUserId: String): String {
        return getOrGenerateAssociationKey(publicUserId)
    }

    override suspend fun onListUniqueAccounts(): Map<String, String> {
        return listAssociationKeys().filter { it.key != ANON_USER }
    }

    override suspend fun onCreateNewKey(publicUserId: String): String {
        return generateAssociationKey(publicUserId)
    }

    suspend fun generateAssociationKey(publicUserId: String): String {
        TelemetryManager.sendEvent(context, TelemetryCodes.ASSOCIATION_KEY_SAVED)

        return withContext(Dispatchers.Main) {
            createECDSAKeypair(ECDSA_KEYPAIR + publicUserId)
        }
    }

    suspend fun listAssociationKeys(): Map<String, String> {
        val aliases =
            withContext(Dispatchers.Main) {
                getKeyStore().aliases()
            }

        return aliases.toList().filter { it.contains(ECDSA_KEYPAIR) }
            .map { it.removePrefix(ECDSA_KEYPAIR) }
            .associateWith { getOrGenerateAssociationKey(it) }
    }

    suspend fun getAssociationKey(publicUserId: String): String? {
        val alias = ECDSA_KEYPAIR + publicUserId

        return withContext(Dispatchers.Main) {
            val keyStore = getKeyStore()

            if (!keyStore.containsAlias(alias)) {
                return@withContext null
            }

            keyStore.getCertificate(alias).publicKey.encoded.toStringBase64()
        }
    }

    suspend fun encryptHkdf(
        backendPublicKey: String,
        data: String,
    ): EncryptionOutput {
        val publicBytes = backendPublicKey.toByteArrayFromBase64String()

        val publicKey =
            if (publicBytes.size <= 65) {
                generateP256PublicKeyFromUncompressedW(publicBytes)
            } else {
                KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
                    .generatePublic(X509EncodedKeySpec(publicBytes)) as ECPublicKey
            }

        TelemetryManager.sendEvent(context, TelemetryCodes.BROWSER_KEY_DERIVED)

        return withContext(Dispatchers.Main) {
            val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC)

            kpg.initialize(ECGenParameterSpec(EC_CURVE))

            val keyPair = kpg.generateKeyPair()
            val keyAgreement = KeyAgreement.getInstance("ECDH")

            keyAgreement.init(keyPair.private)
            keyAgreement.doPhase(publicKey, true)

            computeHkdf(data, keyPair.public.encoded, keyAgreement.generateSecret())
        }
    }

    suspend fun signMessage(
        publicUserId: String,
        message: String,
    ): String {
        val alias = ECDSA_KEYPAIR + publicUserId

        return withContext(Dispatchers.Main) {
            val keyStore = getKeyStore()

            if (!keyStore.containsAlias(alias)) {
                throw NoAssociationKeyPresentException(publicUserId)
            }

            val privateKeyEntry = keyStore.getEntry(alias, null) as KeyStore.PrivateKeyEntry
            val privateKey = privateKeyEntry.privateKey

            val signature = Signature.getInstance(SIGNATURE_ALGORITHM)

            signature.initSign(privateKey)
            signature.update(message.encodeToByteArray())
            signature.sign().toStringBase64()
        }
    }

    suspend fun removeAssociationKey(publicUserId: String) {
        val alias = ECDSA_KEYPAIR + publicUserId

        val keyStore =
            withContext(Dispatchers.Main) {
                getKeyStore().takeIf { it.containsAlias(alias) }
            }

        if (keyStore != null) {
            val key = getOrGenerateAssociationKey(publicUserId)

            withContext(Dispatchers.Main) {
                keyStore.deleteEntry(alias)
            }

            backupService.removeKey(alias)

            TelemetryManager.sendEvent(context, TelemetryCodes.ASSOCIATION_KEY_REMOVED, alias)
        }
    }

    suspend fun waitForRestoring() {
        backupService.waitForAccountsRestoring()
        TelemetryManager.deviceID = getAssociationKey(ANON_USER)
    }

    private suspend fun getOrGenerateAssociationKey(publicUserId: String): String {
        return getAssociationKey(publicUserId) ?: generateAssociationKey(publicUserId)
    }

    private fun createECDSAKeypair(alias: String): String {
        val keyStore = getKeyStore()

        if (keyStore.containsAlias(alias)) {
            val associationKey = keyStore.getCertificate(alias).publicKey.encoded.toStringBase64()

            if (alias == ECDSA_KEYPAIR + ANON_USER) {
                TelemetryManager.deviceID = associationKey
            }

            return associationKey
        }

        val keyPairGenerator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE)

        val keyGenParameterSpec =
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                .setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
                .setDigests(KeyProperties.DIGEST_SHA256).build()

        keyPairGenerator.initialize(keyGenParameterSpec)

        val associationKey = keyPairGenerator.generateKeyPair().public.encoded.toStringBase64()

        backupService.addKey(alias, associationKey)

        if (alias == ECDSA_KEYPAIR + ANON_USER) {
            TelemetryManager.deviceID = associationKey
        }

        TelemetryManager.sendEvent(context, TelemetryCodes.ASSOCIATION_KEY_CREATED, alias)

        return associationKey
    }

    private fun createAnonymousECDSAKeypair(): String {
        return createECDSAKeypair(ECDSA_KEYPAIR + ANON_USER)
    }

    private fun computeHkdf(
        data: String,
        publicKeyBytes: ByteArray,
        secretKeyBytes: ByteArray,
    ): EncryptionOutput {
        val salt = ByteArray(SALT_SIZE)

        val secureRandom =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SecureRandom.getInstanceStrong()
            } else {
                SecureRandom()
            }

        secureRandom.nextBytes(salt)

        val finalKeyBytes = computeHkdf(secretKeyBytes, salt)
        val finalKey = SecretKeySpec(finalKeyBytes, KeyProperties.KEY_ALGORITHM_AES)

        return encrypt(data.encodeToByteArray(), finalKey, publicKeyBytes, salt)
    }

    private fun computeHkdf(
        secretKeyBytes: ByteArray,
        salt: ByteArray,
    ): ByteArray {
        val mac = Mac.getInstance(MAC_ALGORITHM)

        if (salt.isEmpty()) {
            mac.init(SecretKeySpec(ByteArray(mac.macLength), MAC_ALGORITHM))
        } else {
            mac.init(SecretKeySpec(salt, MAC_ALGORITHM))
        }

        val secretKey = mac.doFinal(secretKeyBytes)
        val result = ByteArray(KEY_SIZE_BYTES)
        var counter = 1
        var pos = 0

        mac.init(SecretKeySpec(secretKey, MAC_ALGORITHM))

        var digest = ByteArray(0)

        while (true) {
            mac.update(digest)
            mac.update(byteArrayOf())
            mac.update(counter.toByte())

            digest = mac.doFinal()

            if (pos + digest.size < KEY_SIZE_BYTES) {
                System.arraycopy(digest, 0, result, pos, digest.size)
                pos += digest.size
                counter++
            } else {
                System.arraycopy(digest, 0, result, pos, KEY_SIZE_BYTES - pos)
                break
            }
        }

        return result
    }

    private fun encrypt(
        message: ByteArray,
        key: SecretKey,
        publicKeyBytes: ByteArray,
        salt: ByteArray,
    ): EncryptionOutput {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)

        cipher.init(Cipher.ENCRYPT_MODE, key)

        val iv = cipher.iv.copyOf()
        val cipherText = cipher.doFinal(message)

        return EncryptionOutput(
            publicKeyBytes.toStringBase64(),
            cipherText.toStringBase64(),
            salt.toStringBase64(),
            iv.toStringBase64(),
        )
    }

    private fun getKeyStore(): KeyStore {
        return KeyStore.getInstance(ANDROID_KEYSTORE).also { it.load(null) }
    }

    private fun generateP256PublicKeyFromFlatW(w: ByteArray): ECPublicKey {
        val head = Base64.decode(HEAD_256, Base64.NO_WRAP)
        val encodedKey = ByteArray(head.size + w.size)

        System.arraycopy(head, 0, encodedKey, 0, head.size)
        System.arraycopy(w, 0, encodedKey, head.size, w.size)

        val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
        val keySpec = X509EncodedKeySpec(encodedKey)

        return keyFactory.generatePublic(keySpec) as ECPublicKey
    }

    private fun generateP256PublicKeyFromUncompressedW(w: ByteArray): ECPublicKey {
        if (w[0].toInt() != 0x04) {
            return rawToEncodedECPublicKey(w)
        }

        return generateP256PublicKeyFromFlatW(w.copyOfRange(1, w.size))
    }

    private fun rawToEncodedECPublicKey(rawBytes: ByteArray): ECPublicKey {
        val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
        val x = rawBytes.copyOfRange(0, rawBytes.size / 2)
        val y = rawBytes.copyOfRange(rawBytes.size / 2, rawBytes.size)
        val w = ECPoint(BigInteger(1, x), BigInteger(1, y))

        val keySpec = ECPublicKeySpec(w, ecParameterSpecForCurve())

        return keyFactory.generatePublic(keySpec) as ECPublicKey
    }

    private fun ecParameterSpecForCurve(): ECParameterSpec {
        val params = AlgorithmParameters.getInstance(KeyProperties.KEY_ALGORITHM_EC)

        params.init(ECGenParameterSpec(EC_CURVE))

        return params.getParameterSpec(ECParameterSpec::class.java)
    }

    class EncryptionOutput(
        val publicKey: String,
        val cipherText: String,
        val salt: String,
        val iv: String,
    )

    companion object {
        internal const val ECDSA_KEYPAIR = "ECDSA_KEYPAIR"
        private const val HEAD_256 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE"
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val SIGNATURE_ALGORITHM = "SHA256withECDSA"
        private const val MAC_ALGORITHM = "HMACSHA256"
        private const val EC_CURVE = "prime256v1"

        private const val SALT_SIZE = 12
        private const val KEY_SIZE_BYTES = 32
    }
}

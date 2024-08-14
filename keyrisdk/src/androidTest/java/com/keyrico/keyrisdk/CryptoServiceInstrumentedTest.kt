package com.keyrico.keyrisdk

import android.security.keystore.KeyProperties
import androidx.test.platform.app.InstrumentationRegistry
import com.keyrico.keyrisdk.services.CryptoService
import com.keyrico.keyrisdk.utils.toByteArrayFromBase64String
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.runners.MethodSorters
import java.security.KeyFactory
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(JUnit4::class)
class CryptoServiceInstrumentedTest {
    @Test
    fun `1_testCryptoServiceDifferentKeys`() =
        runBlocking {
            val cryptoService = getCryptoService()

            val publicKey =
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEHT7SM0JL8PPhAOQ+cFJn5hWPPSFKGxbVfp3htpjMUvQ9SM4fhtFUVryoKMz7z5/+MFxW96Sb9FKtq9z7mvJ1w=="
            val rawPublicKey =
                "BBB0+0jNCS/Dz4QDkPnBSZ+YVjz0hShsW1X6d4baYzFL0PUjOH4bRVFa8qCjM+8+f/jBcVvekm/RSravc+5rydc="
            val rawPublicKeyLite =
                "EHT7SM0JL8PPhAOQ+cFJn5hWPPSFKGxbVfp3htpjMUvQ9SM4fhtFUVryoKMz7z5/+MFxW96Sb9FKtq9z7mvJ1w=="

            val data = "Hello World!"

            val publicKeyCipher = cryptoService.encryptHkdf(publicKey, data)
            val rawPublicKeyCipher = cryptoService.encryptHkdf(rawPublicKey, data)
            val rawPublicKeyLiteCipher = cryptoService.encryptHkdf(rawPublicKeyLite, data)

            Assert.assertNotNull(publicKeyCipher)
            Assert.assertNotNull(rawPublicKeyCipher)
            Assert.assertNotNull(rawPublicKeyLiteCipher)
        }

    @Test
    fun `2_testCryptoServiceAssociationKeys`() =
        runBlocking {
            val cryptoService = getCryptoService()

            val anonymousAssociationKey = cryptoService.getAssociationKey("ANON")
            val associationKeyForUnknownUser = cryptoService.generateAssociationKey("Unknown user ID")
            val associationKeyForUnknownUserTwice = cryptoService.getAssociationKey("Unknown user ID")

            cryptoService.generateAssociationKey("User ID")

            val newKeys = cryptoService.listAssociationKeys()

            Assert.assertNotNull(anonymousAssociationKey)
            Assert.assertNotNull(associationKeyForUnknownUser)
            Assert.assertEquals(associationKeyForUnknownUser, associationKeyForUnknownUserTwice)
            Assert.assertNotEquals(0, newKeys.size)

            cryptoService.onListUniqueAccounts().forEach { (alias, _) ->
                cryptoService.removeAssociationKey(alias)
            }

            val keysAfterRemove = cryptoService.listAssociationKeys()

            Assert.assertEquals(1, keysAfterRemove.size) // Only ANON
        }

    @Test
    fun `3_testCryptoServiceUserSignature`() =
        runBlocking {
            val cryptoService = getCryptoService()

            val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
            val signature = Signature.getInstance("SHA256withECDSA")
            val anonUserId = "ANON"

            val anonymousMessageToSign = "Anonymous message to sign"
            cryptoService.generateAssociationKey(anonUserId)
            val anonymousSignedMessage = cryptoService.signMessage(anonUserId, anonymousMessageToSign)
            val anonymousAssociationKey = cryptoService.getAssociationKey(anonUserId)

            val encodedAnonymousKey = anonymousAssociationKey?.toByteArrayFromBase64String()
            val anonymousPublic =
                keyFactory.generatePublic(X509EncodedKeySpec(encodedAnonymousKey)) as ECPublicKey

            signature.initVerify(anonymousPublic)
            signature.update(anonymousMessageToSign.encodeToByteArray())

            val anonymousVerified =
                signature.verify(anonymousSignedMessage.toByteArrayFromBase64String())

            val userMessageToSign = "Message to sign"
            val publicUserId = "public-User-Id"
            cryptoService.generateAssociationKey(publicUserId)
            val userSignedMessage = cryptoService.signMessage(publicUserId, userMessageToSign)
            val userAssociationKey = cryptoService.getAssociationKey(publicUserId)

            val encodedUserKey = userAssociationKey?.toByteArrayFromBase64String()
            val userPublic =
                keyFactory.generatePublic(X509EncodedKeySpec(encodedUserKey)) as ECPublicKey

            signature.initVerify(userPublic)
            signature.update(userMessageToSign.encodeToByteArray())

            val userVerified = signature.verify(userSignedMessage.toByteArrayFromBase64String())

            Assert.assertTrue(anonymousVerified)
            Assert.assertTrue(userVerified)

            cryptoService.removeAssociationKey(publicUserId)
        }

    @Test
    fun `4_testRegisterAndLogin`() =
        runBlocking {
            val keyri = Keyri(InstrumentationRegistry.getInstrumentation().context, "")

            keyri.register("test_publicUserId")

            val loginObject = keyri.login("test_publicUserId").getOrThrow()

            val keyFactory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)
            val signature = Signature.getInstance("SHA256withECDSA")

            val encodedUserKey = loginObject.publicKey.toByteArrayFromBase64String()
            val userPublic =
                keyFactory.generatePublic(X509EncodedKeySpec(encodedUserKey)) as ECPublicKey

            signature.initVerify(userPublic)
            signature.update(loginObject.timestampNonce.encodeToByteArray())

            val messageVerified =
                signature.verify(loginObject.signature.toByteArrayFromBase64String())

            Assert.assertTrue(messageVerified)
        }

    private fun getCryptoService(): CryptoService {
        return CryptoService(InstrumentationRegistry.getInstrumentation().context, "", true)
    }
}

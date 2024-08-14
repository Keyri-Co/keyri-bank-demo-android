package com.keyrico.keyrisdk

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.keyrico.keyrisdk.config.KeyriDetectionsConfig
import com.keyrico.keyrisdk.confirmation.BaseConfirmationBottomDialog
import com.keyrico.keyrisdk.confirmation.ConfirmationBottomDialog
import com.keyrico.keyrisdk.entity.fingerprint.request.FingerprintEventRequest
import com.keyrico.keyrisdk.entity.fingerprint.response.FingerprintEventResponse
import com.keyrico.keyrisdk.entity.login.LoginObject
import com.keyrico.keyrisdk.entity.register.RegisterObject
import com.keyrico.keyrisdk.entity.session.Session
import com.keyrico.keyrisdk.exception.AccountAlreadyExistException
import com.keyrico.keyrisdk.exception.AccountDoesNotExistException
import com.keyrico.keyrisdk.exception.NoAssociationKeyPresentException
import com.keyrico.keyrisdk.sec.NED
import com.keyrico.keyrisdk.sec.SIPD
import com.keyrico.keyrisdk.sec.TDS
import com.keyrico.keyrisdk.sec.checkFakeInstance
import com.keyrico.keyrisdk.sec.checkFakeInvocation
import com.keyrico.keyrisdk.sec.fraud.FraudService
import com.keyrico.keyrisdk.sec.fraud.event.EventType
import com.keyrico.keyrisdk.services.CryptoService
import com.keyrico.keyrisdk.telemetry.TelemetryCodes
import com.keyrico.keyrisdk.telemetry.TelemetryManager
import com.keyrico.keyrisdk.utils.getCorrectedTimestampSeconds
import com.keyrico.keyrisdk.utils.getDeviceId
import com.keyrico.keyrisdk.utils.makeApiCall
import com.keyrico.keyrisdk.utils.provideApiService
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * The `Keyri` class represents Keyri SDK for passwordless QR authentication.
 *
 * @param context - required.
 * @param appKey - appKey for given Origin.
 * @param publicApiKey - optional parameter for fingerprinting, null by default.
 * @param serviceEncryptionKey - optional parameter for fingerprinting, null by default.
 * @param detectionsConfig - configure security checks. See [KeyriDetectionsConfig].
 */
class Keyri
    @JvmOverloads
    constructor(
        private val context: Context,
        private val appKey: String,
        private val publicApiKey: String? = null,
        private val serviceEncryptionKey: String? = null,
        private val detectionsConfig: KeyriDetectionsConfig = KeyriDetectionsConfig(),
    ) {
        @Deprecated(
            message = "Use new constructor with detectionsConfig property.",
            replaceWith =
                ReplaceWith(
                    expression = "Keyri(context, appKey, publicApiKey, serviceEncryptionKey, KeyriDetectionsConfig())",
                    imports = arrayOf("com.keyrico.keyrisdk.config.KeyriDetectionsConfig"),
                ),
            level = DeprecationLevel.WARNING,
        )
        constructor(
            context: Context,
            appKey: String,
            publicApiKey: String? = null,
            serviceEncryptionKey: String? = null,
            blockEmulatorDetection: Boolean,
        ) : this(
            context,
            appKey,
            publicApiKey,
            serviceEncryptionKey,
            KeyriDetectionsConfig(blockEmulatorDetection = blockEmulatorDetection),
        )

        @Volatile
        private var canProcess: Boolean

        init {
            val blockSwizzleDetection = detectionsConfig.blockSwizzleDetection

            checkFakeInstance(blockSwizzleDetection)

            if (!detectionsConfig.blockTamperDetection) {
                performChecksumCheck(context, this)
            }

            if ((!detectionsConfig.blockEmulatorDetection && NED(blockSwizzleDetection).checkNED(context)) ||
                SIPD(detectionsConfig).checkSIPD(context)
            ) {
                canProcess = false

                sendInitEventAndFinishApp()
            } else {
                canProcess = true

                TelemetryManager.sendEvent(context, TelemetryCodes.SDK_INIT)
            }

            TelemetryManager.appKey = appKey
            TelemetryManager.apiKey = publicApiKey
        }

        private val cryptoService =
            CryptoService(context, appKey, detectionsConfig.blockSwizzleDetection)

        /**
         * Returns Base64 public key for the specified publicUserId.
         *
         * @param publicUserId - optional publicUserId, default "ANON" for anonymous users.
         * @return Base64 public key for specified [publicUserId] or error.
         */
        suspend fun generateAssociationKey(publicUserId: String = ANON_USER): Result<String> {
            checkFakeInvocation(detectionsConfig.blockSwizzleDetection)

            return processResultAction(TelemetryCodes.ASSOCIATION_KEY_GENERATED) {
                val userId = publicUserId.takeIf { it.isNotEmpty() } ?: ANON_USER

                cryptoService.generateAssociationKey(userId)
            }
        }

        /**
         * Returns an Base64 ECDSA signature of the optional data with the publicUserId's
         * privateKey (or, if not provided, anonymous privateKey), data can be anything.
         *
         * @param publicUserId - optional publicUserId, default "ANON" for anonymous users.
         * @param data - custom message to sign.
         * @return Base64 ECDSA signature of [data] for specified [publicUserId] or error.
         * If [publicUserId] has no generated association key - [NoAssociationKeyPresentException] will be
         * returned in [Result].
         */
        suspend fun generateUserSignature(
            publicUserId: String = ANON_USER,
            data: String,
        ): Result<String> {
            checkFakeInvocation(detectionsConfig.blockSwizzleDetection)

            return processResultAction(TelemetryCodes.ECDSA_DATA_SIGNED) {
                val userId = publicUserId.takeIf { it.isNotEmpty() } ?: ANON_USER

                cryptoService.signMessage(userId, data)
            }
        }

        /**
         * Returns a map of "association keys" and ECDSA Base64 public keys.
         *
         * @return Map<String, String> aliases of "association keys" and ECDSA Base64 public keys or error.
         */
        suspend fun listAssociationKeys(): Result<Map<String, String>> {
            checkFakeInvocation(detectionsConfig.blockSwizzleDetection)

            return processResultAction(TelemetryCodes.ASSOCIATION_KEY_QUERIED) {
                cryptoService.listAssociationKeys()
            }
        }

        /**
         * Returns a map of unique "association keys" and ECDSA Base64 public keys.
         *
         * @return Map<String, String> aliases of unique "association keys" and ECDSA Base64 public keys or error.
         */
        suspend fun listUniqueAccounts(): Result<Map<String, String>> {
            checkFakeInvocation(detectionsConfig.blockSwizzleDetection)

            return processResultAction(TelemetryCodes.ASSOCIATION_KEY_QUERIED) {
                cryptoService.listAssociationKeys().filter { it.key != ANON_USER }
            }
        }

        /**
         * Returns association Base64 public key for the specified publicUserId's.
         *
         * @param publicUserId - optional publicUserId, default "ANON" for anonymous users.
         * @return Base64 public key if present or null.
         */
        suspend fun getAssociationKey(publicUserId: String = ANON_USER): Result<String?> {
            checkFakeInvocation(detectionsConfig.blockSwizzleDetection)

            return processResultAction(TelemetryCodes.ASSOCIATION_KEY_GET) {
                val userId = publicUserId.takeIf { it.isNotEmpty() } ?: ANON_USER

                cryptoService.getAssociationKey(userId)
            }
        }

        /**
         * Removes association public key for the specified publicUserId's.
         *
         * @param publicUserId - publicUserId.
         * @return [Unit] or error.
         */
        suspend fun removeAssociationKey(publicUserId: String): Result<Unit> {
            checkFakeInvocation(detectionsConfig.blockSwizzleDetection)

            return processResultAction(TelemetryCodes.ASSOCIATION_KEY_DELETED) {
                val userId = publicUserId.takeIf { it.isNotEmpty() } ?: ANON_USER

                if (userId == ANON_USER) {
                    throw IllegalStateException("Can't remove key for $ANON_USER")
                }

                cryptoService.removeAssociationKey(userId)
            }
        }

        /**
         * Sends fingerprint event and event result for specified publicUserId's.
         *
         * @param publicUserId - optional publicUserId, default "ANON" for anonymous users.
         * @param eventType - see [EventType].
         * @param success - result of event.
         * @return [FingerprintEventResponse] or error.
         */
        suspend fun sendEvent(
            publicUserId: String = ANON_USER,
            eventType: EventType,
            success: Boolean,
        ): Result<FingerprintEventResponse> {
            checkFakeInvocation(detectionsConfig.blockSwizzleDetection)

            return processResultAction(TelemetryCodes.EVENT_SENT) {
                TelemetryManager.sendEvent(context, TelemetryCodes.SEND_EVENT)

                val userId = publicUserId.takeIf { it.isNotEmpty() } ?: ANON_USER

                FraudService(detectionsConfig).sendEvent(
                    context,
                    cryptoService,
                    publicApiKey,
                    userId,
                    serviceEncryptionKey,
                    eventType,
                    success,
                ).getOrThrow()
            }
        }

        /**
         * Creates and returns fingerprint event object.
         *
         * @return [FingerprintEventRequest] or error.
         */
        suspend fun createFingerprint(): Result<FingerprintEventRequest> {
            checkFakeInvocation(detectionsConfig.blockSwizzleDetection)

            return processResultAction(TelemetryCodes.CREATE_FINGERPRINT) {
                FraudService(detectionsConfig).getFingerprintEventPayload(
                    context = context,
                    cryptoService = cryptoService,
                    publicApiKey = publicApiKey,
                    serviceEncryptionKey = serviceEncryptionKey,
                ).getOrThrow()
            }
        }

        /**
         * Call it after obtaining the sessionId from QR code or deep link.
         * Returns Session object with Risk attributes (needed to show confirmation screen) or Exception.
         *
         * @param sessionId - scanned sessionId.
         * @param publicUserId - optional publicUserId, "ANON" or null for anonymous users.
         * @return [Session] for specified [sessionId] or error.
         */
        suspend fun initiateQrSession(
            sessionId: String,
            publicUserId: String?,
        ): Result<Session> {
            val blockSwizzleDetection = detectionsConfig.blockSwizzleDetection

            checkFakeInvocation(blockSwizzleDetection)

            return processResultAction(TelemetryCodes.GET_RESPONSE_HANDLED) {
                val userId = publicUserId.takeIf { it?.isNotEmpty() == true } ?: ANON_USER

                TelemetryManager.lastSessionId = sessionId

                val androidSecureId = context.getDeviceId(blockSwizzleDetection)
                val associationKey = cryptoService.generateAssociationKey(userId)

                TelemetryManager.sendEvent(context, TelemetryCodes.GET_TRIGGERED)

                val session =
                    makeApiCall(context, blockSwizzleDetection) {
                        provideApiService(blockSwizzleDetection).getSession(
                            associationKey,
                            androidSecureId,
                            sessionId,
                            appKey,
                        )
                    }.map { it.toSession(userId, appKey, publicApiKey, blockSwizzleDetection) }

                if (session.isFailure) {
                    TelemetryManager.lastSessionId = null
                }

                session.getOrThrow()
            }
        }

        /**
         * Call it to create [LoginObject] for login.
         *
         * @param publicUserId - optional publicUserId, "ANON" or null for anonymous users.
         * @return [LoginObject] with timestamp_nonce, signature, publicKey, userId.
         */
        suspend fun login(publicUserId: String?): Result<LoginObject> {
            checkFakeInvocation(detectionsConfig.blockSwizzleDetection)

            return processResultAction(TelemetryCodes.LOGIN) {
                val userId = publicUserId?.takeIf { it.isNotEmpty() } ?: ANON_USER
                val publicKey =
                    cryptoService.getAssociationKey(userId)
                        ?: throw AccountDoesNotExistException(userId)

                val timestampSeconds = getCorrectedTimestampSeconds(context)
                val nonce = Random.nextInt(1000..9999).toString()

                val timestampNonce = "${timestampSeconds}_$nonce"
                val signature = cryptoService.signMessage(userId, timestampNonce)

                LoginObject(timestampNonce, signature, publicKey, userId)
            }
        }

        /**
         * Call it to create [RegisterObject] for register.
         *
         * @param publicUserId - optional publicUserId, "ANON" or null for anonymous users.
         * @return [RegisterObject] with publicKey, userId.
         */
        suspend fun register(publicUserId: String?): Result<RegisterObject> {
            checkFakeInvocation(detectionsConfig.blockSwizzleDetection)

            return processResultAction(TelemetryCodes.REGISTER) {
                val userId = publicUserId?.takeIf { it.isNotEmpty() } ?: ANON_USER

                if (cryptoService.getAssociationKey(userId) != null) {
                    throw AccountAlreadyExistException(userId)
                }

                val publicKey = cryptoService.generateAssociationKey(userId)

                RegisterObject(publicKey, userId)
            }
        }

        /**
         * Call it to show Confirmation with default UI. Returns Boolean result.
         *
         * @param fragmentManager - activity's fragmentManager to show bottom sheet.
         * @param session - previously retrieved [Session] object.
         * @param payload - payload can be anything (session token or a stringified JSON containing multiple items.
         * Can include things like publicUserId, timestamp, data and ECDSA signature).
         * @return [Unit] for session confirmation or error.
         */
        suspend fun initializeDefaultConfirmationScreen(
            fragmentManager: FragmentManager,
            session: Session,
            payload: String,
        ): Result<Unit> {
            val blockSwizzleDetection = detectionsConfig.blockSwizzleDetection

            checkFakeInvocation(blockSwizzleDetection)

            if (!canProcess) {
                return Result.failure(IllegalStateException("Can't perform initializeDefaultConfirmationScreen action. SDK check failed."))
            }

            return callbackFlow {
                var callback: ((Result<Unit>) -> Unit)? = { trySend(it) }

                fragmentManager.fragments.filterIsInstance<BaseConfirmationBottomDialog>().forEach {
                    it.silentDismiss = true

                    fragmentManager.beginTransaction().remove(it).commit()
                }

                ConfirmationBottomDialog(session, payload, blockSwizzleDetection, callback)
                    .show(fragmentManager, ConfirmationBottomDialog::class.java.name)

                awaitClose { callback = null }
            }.first()
        }

        /**
         * Call it to process scanned [url] with sessionId and show Confirmation with default UI.
         *
         * @param fragmentManager - activity's fragmentManager to show bottom sheet.
         * @param url - url from scanned QR code.
         * @param payload - Payload can be anything (session token or a stringified JSON containing multiple items.
         * Can include things like publicUserId, timestamp, data and ECDSA signature).
         * @param publicUserId - optional publicUserId, "ANON" or null for anonymous users.
         * @return [Unit] for session confirmation or error.
         */
        suspend fun processLink(
            fragmentManager: FragmentManager,
            url: Uri,
            payload: String,
            publicUserId: String?,
        ): Result<Unit> {
            checkFakeInvocation(detectionsConfig.blockSwizzleDetection)

            return try {
                val sessionId =
                    url.getQueryParameters("sessionId")?.firstOrNull()
                        ?: throw Exception("Failed to process link")

                val session = initiateQrSession(sessionId, publicUserId).getOrThrow()

                initializeDefaultConfirmationScreen(fragmentManager, session, payload)
            } catch (exception: Exception) {
                TelemetryManager.sendEvent(context, exception)
                Result.failure(exception)
            }
        }

        private suspend fun <T> processResultAction(
            code: TelemetryCodes,
            actionBlock: suspend () -> T,
        ): Result<T> {
            return try {
                if (!canProcess) {
                    throw IllegalStateException("Can't perform ${code.codeName} action. SDK check failed.")
                }

                cryptoService.waitForRestoring()

                val actionResult = actionBlock()

                TelemetryManager.sendEvent(context, code)
                Result.success(actionResult)
            } catch (e: Exception) {
                TelemetryManager.sendEvent(context, e)
                Result.failure(e)
            }
        }

        @OptIn(DelicateCoroutinesApi::class)
        private fun sendInitEventAndFinishApp() {
            GlobalScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, DEVICE_COMPROMISED_MESSAGE, Toast.LENGTH_LONG).show()
                }

                delay(2_000L)

                val error = RuntimeException(DEVICE_COMPROMISED_MESSAGE)

                TelemetryManager.sendEvent(context, TelemetryCodes.SDK_INIT, error)

                throw error
            }
        }

        companion object {
            const val KEYRI_KEY = "Keyri"
            const val KEYRI_PLATFORM = "Android"
            const val ANON_USER = "ANON"
            const val BACKUP_KEYRI_PREFS = "keyri_backup_prefs"
            private const val DEVICE_COMPROMISED_MESSAGE = "Your device was compromised"

            private fun performChecksumCheck(
                context: Context,
                keyri: Keyri,
            ) {
                @OptIn(DelicateCoroutinesApi::class)
                GlobalScope.launch(Dispatchers.IO) {
                    TDS(keyri.detectionsConfig.blockSwizzleDetection).checksumCheck(
                        context,
                        keyri.appKey,
                    ).onFailure {
                        keyri.canProcess = false

                        keyri.sendInitEventAndFinishApp()
                    }
                }
            }
        }
    }

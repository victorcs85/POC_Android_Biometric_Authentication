package br.com.victorcs.biometricauth.biometric

import android.content.Context
import android.icu.text.CaseMap
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import br.com.victorcs.biometricauth.data.crypto.EncryptedData
import br.com.victorcs.biometricauth.data.crypto.EncryptionType
import br.com.victorcs.biometricauth.data.crypto.ICryptographyManager

abstract class BiometricAuthenticator(
    activity: FragmentActivity,
    protected val listener: BiometricAuthenticatorListener,
    protected val buildHelper: BiometricBuildHelper
) {

    var isDeviceCredentialAuthenticationEnabled = false
    var isStrongAuthenticationEnabled = false
    var isWeakAuthenticationEnabled = false
    var showAuthenticationConfirmation = false

    /** Handle using biometrics + cryptography to encrypt/decrypt data securely */
    protected val cryptographyManager = ICryptographyManager.instance()
    protected lateinit var encryptionType: EncryptionType
    protected lateinit var encryptedData: EncryptedData

    private var title = buildHelper.title
    private var subtitle: String? = buildHelper.subtitle
    private var description: String? = buildHelper.description
    private var btnCancelDescription: String? = buildHelper.btnCancelDescription

    /** Receives callbacks from an authentication operation */
    private val authenticationCallback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            listener.onLibMessageResponse("Authentication succeeded")

            val type = result.authenticationType
            val cryptoObject = result.cryptoObject
            listener.onLibMessageResponse("Type: ${getAuthenticationType(type)} - Crypto: $cryptoObject")

            val cipher = cryptoObject?.cipher ?: return

            when (encryptionType) {
                EncryptionType.ENCRYPT -> {
                    encryptedData = cryptographyManager.encrypt(PAYLOAD, cipher)
//                    listener.onLibMessageResponse("Encrypted text: ${encryptedData.encrypted}")
                    buildHelper.onAuthErrorAction.invoke()
                }
                EncryptionType.DECRYPT -> {
                    val plainData = cryptographyManager.decrypt(encryptedData.encrypted, cipher)
//                    listener.onLibMessageResponse("Decrypted text: $plainData")
                    buildHelper.onAuthErrorAction.invoke()
                }
            }
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            listener.onLibMessageResponse("Authentication error[${getBiometricError(errorCode)}] - $errString")
        }

        override fun onAuthenticationFailed() {
            listener.onLibMessageResponse("Authentication failed - Biometric is valid but not recognized")
        }
    }

    /** Manages a biometric prompt, and allows to perform an authentication operation */
    protected val biometricPrompt =
        BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), authenticationCallback)

    abstract fun canAuthenticate(context: Context): Boolean

    fun authenticateWithoutCrypto(context: Context) {
        val promptInfo = buildPromptInfo(context) ?: return
        biometricPrompt.authenticate(promptInfo)
    }

    abstract fun authenticateAndEncrypt(context: Context)

    abstract fun authenticateAndDecrypt(context: Context)

    abstract fun setAllowedAuthenticators(builder: PromptInfo.Builder)

    fun cancelAuthentication() {
        biometricPrompt.cancelAuthentication()
    }

    /** Build a [PromptInfo] that defines the properties of the biometric prompt dialog. */
    protected fun buildPromptInfo(context: Context): PromptInfo? {
        val builder = PromptInfo.Builder()
            .setTitle(title)

        subtitle?.let {
            builder.setSubtitle(it)
        }
        description?.let {
            builder.setDescription(it)
        }

        // Show a confirmation button after authentication succeeds
        builder.setConfirmationRequired(showAuthenticationConfirmation)

        // Allow authentication with a password, pin or pattern
        setAllowedAuthenticators(builder)

        // Set a negative button. It would typically display "Cancel"
        btnCancelDescription?.let {
            builder.setNegativeButtonText(it)
        }

        return try {
            builder.build()
        } catch (exception: IllegalArgumentException) {
            Log.e(
                BiometricAuthenticator::class.simpleName.orEmpty(),
                "Building prompt info error - ${exception.message}"
            )
            null
        }
    }

    companion object {
        private const val PAYLOAD = "Biometrics sample"

        fun instance(
            activity: FragmentActivity,
            listener: BiometricAuthenticatorListener,
            buildHelper: BiometricBuildHelper
        ): BiometricAuthenticator {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return BiometricAuthenticatorLegacy(
                    activity,
                    listener,
                    buildHelper
                )
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                return BiometricAuthenticatorApi23(
                    activity,
                    listener,
                    buildHelper
                )
            }
            return BiometricAuthenticatorApi30(
                activity,
                listener,
                buildHelper
            )
        }
    }
}
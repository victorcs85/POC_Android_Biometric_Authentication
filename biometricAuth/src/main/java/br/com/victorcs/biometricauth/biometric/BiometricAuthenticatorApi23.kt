package br.com.victorcs.biometricauth.biometric

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import br.com.victorcs.biometricauth.data.crypto.EncryptionType
import java.lang.Exception

@RequiresApi(Build.VERSION_CODES.M)
class BiometricAuthenticatorApi23(
    activity: FragmentActivity,
    listener: BiometricAuthenticatorListener,
    buildHelper: BiometricBuildHelper
) :
    BiometricAuthenticator(
        activity,
        listener,
        buildHelper
    ) {

    @Suppress("DEPRECATION")
    override fun canAuthenticate(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate()
        listener.onLibMessageResponse(getBiometricAvailability(canAuthenticate))
        return true.takeIf { canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS } ?: false
    }

    override fun authenticateAndEncrypt(context: Context) {
        try {

            val promptInfo = buildPromptInfo(context) ?: return
            val cipher = cryptographyManager.getCipherForEncryption()
            val crypto = BiometricPrompt.CryptoObject(cipher)
            encryptionType = EncryptionType.ENCRYPT
            biometricPrompt.authenticate(promptInfo, crypto)

        } catch (exception: IllegalArgumentException) {
            listener.onLibMessageResponse("Authentication with crypto error - ${exception.message}")
        }
    }

    override fun authenticateAndDecrypt(context: Context) {
        try {

            val promptInfo = buildPromptInfo(context) ?: return
            val cipher =
                cryptographyManager.getCipherForDecryption(encryptedData.initializationVector)
            val crypto = BiometricPrompt.CryptoObject(cipher)
            encryptionType = EncryptionType.DECRYPT
            biometricPrompt.authenticate(promptInfo, crypto)

        } catch (exception: IllegalArgumentException) {
            listener.onLibMessageResponse("Authentication with crypto error - ${exception.message}")
        }
    }

    @Suppress("DEPRECATION")
    override fun setAllowedAuthenticators(builder: BiometricPrompt.PromptInfo.Builder) {
        builder.setDeviceCredentialAllowed(isDeviceCredentialAuthenticationEnabled)
    }
}

package br.com.victorcs.biometricauth.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

class BiometricAuthenticatorLegacy(
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
        listener.onLibMessageResponse("Cannot use biometrics with cryptography on this API level.")
    }

    override fun authenticateAndDecrypt(context: Context) {
        listener.onLibMessageResponse("Cannot use biometrics with cryptography on this API level.")
    }

    @Suppress("DEPRECATION")
    override fun setAllowedAuthenticators(builder: BiometricPrompt.PromptInfo.Builder) {
        builder.setDeviceCredentialAllowed(isDeviceCredentialAuthenticationEnabled)
    }
}
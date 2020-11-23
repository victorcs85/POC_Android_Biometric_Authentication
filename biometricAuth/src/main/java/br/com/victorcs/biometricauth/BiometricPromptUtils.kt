package br.com.victorcs.biometricauth

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class BiometricPromptUtils: IBiometricPrompt {

    companion object {
        private const val TAG = "BiometricPromptUtils"
    }

    override fun createBiometricPrompt(
        activity: AppCompatActivity,
        processError: (Int) -> Unit?,
        processFailed: () -> Unit?,
        processSuccess: (BiometricPrompt.AuthenticationResult) -> Unit?
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                Log.d(TAG, "Code error: $errCode - Message error: $errString")
                processError(errCode)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d(TAG, "Biometric authentication failed for unknown reason.")
                processFailed()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d(TAG, "Authentication was successful")
                processSuccess(result)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    override fun createPromptInfo(
        activity: AppCompatActivity,
        useFacial: Boolean
    ): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(activity.getString(R.string.prompt_info_title))
            setSubtitle(activity.getString(R.string.prompt_info_subtitle))
            setConfirmationRequired(false)
            if(useFacial) {
                setDeviceCredentialAllowed(true)//via facial
            } else {
                setNegativeButtonText(activity.getString(R.string.prompt_info_use_app_password))
            }
        }.build()
}
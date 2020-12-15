package br.com.victorcs.poc_biometria.refactor

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.com.victorcs.biometricauth.biometric.BiometricAuthenticator
import br.com.victorcs.biometricauth.biometric.BiometricAuthenticatorListener
import br.com.victorcs.biometricauth.biometric.BiometricBuildHelper


class MainActivity : AppCompatActivity() {

    private lateinit var biometricAuthenticator: BiometricAuthenticator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        biometricAuthenticator =
            BiometricAuthenticator.instance(this, object : BiometricAuthenticatorListener {
                override fun onLibMessageResponse(message: String) {
                    log(message)
                }
            },
                BiometricBuildHelper(
                    title = "Title",
                    subtitle = "Subtitle",
                    description = "Description",
                    btnCancelDescription = "Cancel",
                    onAuthErrorAction = {},
                    onAuthFailureAction = {},
                    onAuthSuccessAction = {}
                )
            )

/*        canAuthenticate.setOnClickListener { biometricAuthenticator.canAuthenticate(this) }
        authenticate.setOnClickListener { biometricAuthenticator.authenticateWithoutCrypto(this) }
        authenticateEncrypt.setOnClickListener { biometricAuthenticator.authenticateAndEncrypt(this) }
        authenticateDecrypt.setOnClickListener { biometricAuthenticator.authenticateAndDecrypt(this) }

        // Check =box listeners
        authenticatorStrong.setOnCheckedChangeListener { _, isChecked ->
            biometricAuthenticator.isStrongAuthenticationEnabled = isChecked
        }
        authenticatorWeak.setOnCheckedChangeListener { _, isChecked ->
            biometricAuthenticator.isWeakAuthenticationEnabled = isChecked
        }
        authenticatorDeviceCredential.setOnCheckedChangeListener { _, isChecked ->
            biometricAuthenticator.isDeviceCredentialAuthenticationEnabled = isChecked
        }
        negativeButton.setOnCheckedChangeListener { _, isChecked ->
            biometricAuthenticator.showNegativeButton = isChecked
        }
        authenticationConfirmation.setOnCheckedChangeListener { _, isChecked ->
            biometricAuthenticator.showAuthenticationConfirmation = isChecked
        }

        // Initial states
        biometricAuthenticator.isStrongAuthenticationEnabled = authenticatorStrong.isChecked
        biometricAuthenticator.isWeakAuthenticationEnabled = authenticatorWeak.isChecked
        biometricAuthenticator.isDeviceCredentialAuthenticationEnabled =
            authenticatorDeviceCredential.isChecked
        biometricAuthenticator.showNegativeButton = negativeButton.isChecked
        biometricAuthenticator.showAuthenticationConfirmation = authenticationConfirmation.isChecked

        clearLogs.setOnClickListener { clearLogs() }*/
    }

    override fun onStop() {
        super.onStop()
        if (isChangingConfigurations && !keepAuthenticationDialogOnConfigurationChange()) {
            biometricAuthenticator.cancelAuthentication()
        }
    }

    private fun keepAuthenticationDialogOnConfigurationChange(): Boolean {
//        return configurationChange.isChecked
        return false
    }

    @SuppressLint("SetTextI18n")
    private fun log(message: String) {
//        val currentLogs = logs.text.toString()
//        logs.text = "$message\n$currentLogs"
    }

    private fun clearLogs() {
//        logs.text = ""
    }
}
package br.com.victorcs.biometricauth.biometric

interface BiometricAuthenticatorListener {
    fun onLibMessageResponse(message: String)
}
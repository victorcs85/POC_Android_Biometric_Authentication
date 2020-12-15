package br.com.victorcs.biometricauth.biometric

data class BiometricBuildHelper(
    val title: String,
    val subtitle: String?,
    val description: String?,
    val btnCancelDescription: String?,
    val onAuthSuccessAction: () -> Unit,
    val onAuthFailureAction: () -> Unit,
    val onAuthErrorAction: () -> Unit
)
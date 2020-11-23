package br.com.victorcs.biometricauth

import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt

interface IBiometricPrompt {
    fun createBiometricPrompt(
        activity: AppCompatActivity,
        processError: (Int) -> Unit?,
        processFailed: () -> Unit?,
        processSuccess: (BiometricPrompt.AuthenticationResult) -> Unit?
    ): BiometricPrompt

    fun createPromptInfo(activity: AppCompatActivity, useFacial: Boolean): BiometricPrompt.PromptInfo
}
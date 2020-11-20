package br.com.victorcs.poc_biometria.utils

import android.content.Context
import androidx.biometric.BiometricManager
import br.com.victorcs.poc_biometria.R
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseUtils : IFirebaseUtils {
    override fun logNonFatalLog(error: Throwable) {
        FirebaseCrashlytics.getInstance().apply {
            recordException(error)
            sendUnsentReports()
        }
    }

    override fun logNonFatalLog(message: String) {
        FirebaseCrashlytics.getInstance().apply {
            log(message)
            sendUnsentReports()
        }
    }

    override fun logBiometricCanAuthenticateReturn(result: Int) {
        when (result) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                logNonFatalLog("App can authenticate using biometrics.")
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                logNonFatalLog(
                    "No biometric features available on this device."
                )
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                logNonFatalLog(
                    "Biometric features are currently unavailable."
                )
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                logNonFatalLog(
                    "The user hasn't associated " +
                            "any biometric credentials with their account."
                )
        }
    }
}
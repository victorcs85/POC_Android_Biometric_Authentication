package br.com.victorcs.poc_biometria.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseUtils: IFirebaseUtils {
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
}
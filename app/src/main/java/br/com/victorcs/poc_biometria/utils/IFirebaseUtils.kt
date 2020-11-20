package br.com.victorcs.poc_biometria.utils

import android.content.Context

interface IFirebaseUtils {
    fun logNonFatalLog(error: Throwable)
    fun logNonFatalLog(message: String)
    fun logBiometricCanAuthenticateReturn(result: Int)
}
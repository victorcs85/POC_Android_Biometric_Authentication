package br.com.victorcs.poc_biometria.utils

interface IFirebaseUtils {
    fun logNonFatalLog(error: Throwable)
    fun logNonFatalLog(message: String)
}
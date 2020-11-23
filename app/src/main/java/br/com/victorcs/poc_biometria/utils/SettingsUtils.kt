package br.com.victorcs.poc_biometria.utils

import android.content.Context
import android.content.SharedPreferences

object SettingsUtils {
    private fun getSharedPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)

    fun getUseBiometricSettings(context: Context): Boolean {
        val sharedPreferences = getSharedPrefs(context)
        return sharedPreferences.getBoolean(SHARED_PREFS_USE_BIOMETRIC_SETTINGS, true)
    }

    fun getUseBiometricFacial(context: Context): Boolean {
        val sharedPreferences = getSharedPrefs(context)
        return sharedPreferences.getBoolean(SHARED_PREFS_USE_BIOMETRIC_FACIAL, false)
    }

    fun updateUseBiometricSettings(context: Context, booleanValue: Boolean) {
        getSharedPrefs(context).edit()
            .putBoolean(SHARED_PREFS_USE_BIOMETRIC_SETTINGS, booleanValue).apply()
    }

    fun updateUseBiometricFacial(context: Context, booleanValue: Boolean) {
        getSharedPrefs(context).edit()
            .putBoolean(SHARED_PREFS_USE_BIOMETRIC_FACIAL, booleanValue).apply()
    }
}
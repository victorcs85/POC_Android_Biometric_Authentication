package br.com.victorcs.poc_biometria.view.base

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import br.com.victorcs.poc_biometria.R
import br.com.victorcs.poc_biometria.utils.ChangedBiometricUtils
import br.com.victorcs.poc_biometria.utils.SettingsUtils
import br.com.victorcs.poc_biometria.utils.postDelay
import br.com.victorcs.poc_biometria.view.settings.SettingsActivity
import kotlin.system.exitProcess

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun callSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    fun getInfoResult(): String {
        var result = getString(R.string.results)
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        val hasBiometric =
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS)
                getString(R.string.available) else getString(R.string.no)
        val settingsResult = if (SettingsUtils.getUseBiometricSettings(this))
            getString(R.string.enabled) else getString(R.string.block)
        result = result.plus(getString(R.string.biometric_available, hasBiometric)).plus("\n")
        result = result.plus(getString(R.string.biometric_app_enabled, settingsResult))

        return result
    }

    fun getInfoIds(): String {
        var result = getString(R.string.finger_ids)
        checkFingerIdChanges()?.forEach {
            result = result.plus(it + "\n")
        }
        return result
    }

    fun exitApp() {
        finish()
        postDelay({
            exitProcess(0)
        }, 500)
    }

    //region use case 1, check change finger has changed in the SO- test
    private fun checkFingerIdChanges(): MutableList<String>? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ChangedBiometricUtils.getFingerprintInfo(this)
        }
        return null
    }
    //endregion
}
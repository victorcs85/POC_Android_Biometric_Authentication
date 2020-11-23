package br.com.victorcs.poc_biometria.view.settings

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import br.com.victorcs.biometricauth.data.repository.CryptographyManager
import br.com.victorcs.poc_biometria.R
import br.com.victorcs.poc_biometria.utils.SHARED_PREFS_FILENAME
import br.com.victorcs.poc_biometria.utils.SampleAppUser
import br.com.victorcs.poc_biometria.utils.SettingsUtils
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.toolbar.*
import timber.log.Timber

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        toolbar?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        use_biometric?.apply {
            val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()

            isChecked =
                (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS &&
                        SettingsUtils.getUseBiometricSettings(this@SettingsActivity))

            setOnCheckedChangeListener { _, isChecked ->
                if (!isChecked) {
                    try {
                        CryptographyManager().clear(
                            getString(R.string.secret_key_name),
                            applicationContext,
                            SHARED_PREFS_FILENAME,
                            Context.MODE_PRIVATE
                        )
                        SampleAppUser.username = null
                        SampleAppUser.fakeToken = null
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
                SettingsUtils.updateUseBiometricSettings(
                    this@SettingsActivity,
                    isChecked
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else ->
            super.onOptionsItemSelected(item)
    }
}
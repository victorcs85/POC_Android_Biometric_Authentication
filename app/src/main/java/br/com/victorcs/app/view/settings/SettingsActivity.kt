package br.com.victorcs.app.view.settings

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import br.com.victorcs.app.R
import br.com.victorcs.app.utils.SettingsUtils
import br.com.victorcs.biometricauth.CryptographyManager
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.toolbar.*
import java.lang.Exception

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
                        SettingsUtils.loadUseBiometricSettings(this@SettingsActivity))

            setOnCheckedChangeListener { _, isChecked ->
                SettingsUtils.updateUseBiometricSettings(
                    this@SettingsActivity,
                    isChecked
                )

                if(!isChecked) {
                    try {
                        CryptographyManager().clear(getString(R.string.secret_key_name))
                    } catch (e: Exception) {
                        Log.e("ERRO", e.toString())
                    }
                }
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
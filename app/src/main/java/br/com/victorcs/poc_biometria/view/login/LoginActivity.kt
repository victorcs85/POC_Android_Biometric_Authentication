package br.com.victorcs.poc_biometria.view.login

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import br.com.victorcs.biometricauth.IBiometricPrompt
import br.com.victorcs.biometricauth.data.repository.ICryptographyManager
import br.com.victorcs.poc_biometria.R
import br.com.victorcs.poc_biometria.utils.*
import br.com.victorcs.poc_biometria.view.enable.EnableBiometricLoginActivity
import br.com.victorcs.poc_biometria.view.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class LoginActivity : AppCompatActivity(), ILoginContract.View {
    private lateinit var biometricPrompt: BiometricPrompt
    private val cipherTextWrapper
        get() = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
            applicationContext,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            CIPHER_TEXT_WRAPPER
        )

    private val presenter by inject<ILoginContract.Presenter> { parametersOf(this) }
    private val biometricPromptUtils by inject<IBiometricPrompt> { parametersOf(this) }
    private val cryptographyManager by inject<ICryptographyManager> { parametersOf(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        presenter.init()
//        Log.d("StrongBiometrics", ""+canAuthenticateWithStrongBiometrics())
    }

    override fun onResume() {
        super.onResume()

        if (cipherTextWrapper != null) {
            if (SampleAppUser.fakeToken == null) {
                showBiometricPromptForDecryption()
            } else {
                updateApp(getString(R.string.already_signedin))
            }
        }

        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        setupUseBiometricVisibility(canAuthenticate)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun setupView() {
        toolbar?.let {
            setSupportActionBar(it)
        }

        login?.setOnClickListener {
            presenter.validateLogin(
                username?.text?.toString().orEmpty(),
                password?.text?.toString().orEmpty()
            )
        }

        username?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                presenter.validateUser(editable?.toString().orEmpty())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        password?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                presenter.validatePass(editable?.toString().orEmpty())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })


        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            without_biometrics?.visibility = View.GONE

            setupUseBiometricVisibility(canAuthenticate)

            use_biometrics?.setOnClickListener {
                if (cipherTextWrapper != null) {
                    showBiometricPromptForDecryption()
                } else {
                    callEnableBiometric()
                }
            }
        } else {
            use_biometrics?.visibility = View.GONE
            without_biometrics?.visibility = View.VISIBLE
        }

        info?.text = setupInfoResult()
    }

    override fun showUserError() {
        username?.error = getString(R.string.username_error)
    }

    override fun showPassError() {
        password?.error = getString(R.string.password_error)
    }

    override fun hideUserError() {
        username?.error = null
    }

    override fun hidePassError() {
        password?.error = null
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            callSettings()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun callSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun showBiometricPromptForDecryption() {
        cipherTextWrapper?.let { textWrapper ->
            val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                val secretKeyName = getString(R.string.secret_key_name)
                val cipher = cryptographyManager.getInitializedCipherForDecryption(
                    secretKeyName, textWrapper.initializationVector, this@LoginActivity,
                    getString(R.string.secret_key_name),
                    Context.MODE_PRIVATE
                ) {
                    callEnableBiometric()
                    return@getInitializedCipherForDecryption
                }
                if (cipher != null) {
                    biometricPrompt =
                        biometricPromptUtils.createBiometricPrompt(
                            this@LoginActivity,
                            ::decryptServerTokenFromStorage
                        )
                    val promptInfo = biometricPromptUtils.createPromptInfo(this@LoginActivity)
                    try {
                        biometricPrompt.authenticate(
                            promptInfo,
                            BiometricPrompt.CryptoObject(cipher)
                        )
                    } catch (e: Exception) {
                        Timber.e(e.toString())
                        showToast(e.toString())
                    }
                }
            }
        }
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        try {
            cipherTextWrapper?.let { textWrapper ->
                authResult.cryptoObject?.cipher?.let {
                    val plaintext =
                        cryptographyManager.decryptData(textWrapper.ciphertext, it)
                    SampleAppUser.fakeToken = plaintext
                    updateApp(getString(R.string.already_signedin))
                }
            }
        } catch (e: java.lang.Exception) {
            Timber.e(e.toString())
            showToast(e.toString())
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateApp(successMsg: String) {
        success?.text = successMsg.plus(" ${SampleAppUser.fakeToken}")
        scrollToDown()
    }

    private fun scrollToDown() {
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun setupInfoResult(): String {
        var result = "Resultados:\n"
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        val hasBiometric =
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) "DISPONÍVEL" else "NÃO"
        result = result.plus("Biometria disponível? $hasBiometric\n")
        checkFingerIdChanges()?.forEach {
            result = result.plus(it + "\n")
        }

        return result
    }

    private fun callEnableBiometric() {
        startActivity(Intent(this, EnableBiometricLoginActivity::class.java))
    }

    private fun setupUseBiometricVisibility(canAuthenticate: Int) {
        use_biometrics?.visibility =
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS &&
                SettingsUtils.loadUseBiometricSettings(this@LoginActivity)
            )
                View.VISIBLE else View.GONE
    }

    //region use case 1, check change finger has changed in the SO- test
    private fun checkFingerIdChanges(): MutableList<String>? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ChangedBiometricUtils.getFingerprintInfo(this)
        }
        return null
    }
    //endregion

    //test faceID - only on biometric 1.1.0-beta01, without work face id at 9.0
    /* private fun canAuthenticateWithStrongBiometrics(): Boolean {
         return BiometricManager.from(this)
             .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
     }*/
    //endregion
}
package br.com.victorcs.app.view.login

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import br.com.victorcs.app.R
import br.com.victorcs.app.utils.ChangeBiometricUtils
import br.com.victorcs.app.utils.SampleAppUser
import br.com.victorcs.app.view.enable.EnableBiometricLoginActivity
import br.com.victorcs.biometricauth.BiometricPromptUtils
import br.com.victorcs.biometricauth.CIPHERTEXT_WRAPPER
import br.com.victorcs.biometricauth.CryptographyManager
import br.com.victorcs.biometricauth.SHARED_PREFS_FILENAME
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class LoginActivity : AppCompatActivity(), ILoginContract.View {
    private lateinit var biometricPrompt: BiometricPrompt
    private val cryptographyManager = CryptographyManager()
    private val ciphertextWrapper
        get() = cryptographyManager.getCiphertextWrapperFromSharedPrefs(
            applicationContext,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            CIPHERTEXT_WRAPPER
        )

    private val presenter by inject<ILoginContract.Presenter> { parametersOf(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        presenter.init()
        checkFingerIdChanges()
    }

    override fun onResume() {
        super.onResume()

        if (ciphertextWrapper != null) {
            if (SampleAppUser.fakeToken == null) {
                showBiometricPromptForDecryption()
            } else {
                updateApp(getString(R.string.already_signedin))
            }
        }
    }

    override fun setupView() {
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
            use_biometrics?.visibility = View.VISIBLE
            use_biometrics?.setOnClickListener {
                if (ciphertextWrapper != null) {
                    showBiometricPromptForDecryption()
                } else {
                    startActivity(Intent(this, EnableBiometricLoginActivity::class.java))
                }
            }
        } else {
            use_biometrics?.visibility = View.GONE
            without_biometrics?.visibility = View.VISIBLE
        }
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

    private fun showBiometricPromptForDecryption() {
        ciphertextWrapper?.let { textWrapper ->
            val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                val secretKeyName = getString(R.string.secret_key_name)
                val cipher = cryptographyManager.getInitializedCipherForDecryption(
                    secretKeyName, textWrapper.initializationVector
                )
                biometricPrompt =
                    BiometricPromptUtils.createBiometricPrompt(
                        this,
                        ::decryptServerTokenFromStorage
                    )
                val promptInfo = BiometricPromptUtils.createPromptInfo(this)
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        ciphertextWrapper?.let { textWrapper ->
            authResult.cryptoObject?.cipher?.let {
                val plaintext =
                    cryptographyManager.decryptData(textWrapper.ciphertext, it)
                SampleAppUser.fakeToken = plaintext
                updateApp(getString(R.string.already_signedin))
            }
        }
    }

    private fun updateApp(successMsg: String) {
        success?.text = successMsg
    }

    //region use case 1, check change finger has changed in the SO- test
    private fun checkFingerIdChanges() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ChangeBiometricUtils.getFingerprintInfo(this)
        }
    }
    //endregion
}
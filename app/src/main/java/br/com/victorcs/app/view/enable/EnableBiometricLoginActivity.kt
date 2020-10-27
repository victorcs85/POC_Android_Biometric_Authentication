package br.com.victorcs.app.view.enable

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import br.com.victorcs.app.R
import br.com.victorcs.app.utils.ChangeBiometricUtils
import br.com.victorcs.app.utils.SampleAppUser
import br.com.victorcs.biometricauth.BiometricPromptUtils
import br.com.victorcs.biometricauth.CIPHERTEXT_WRAPPER
import br.com.victorcs.biometricauth.CryptographyManager
import br.com.victorcs.biometricauth.SHARED_PREFS_FILENAME
import kotlinx.android.synthetic.main.activity_enable_biometric_login.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

class EnableBiometricLoginActivity : AppCompatActivity(), IEnableBiometricLoginContract.View {
    private lateinit var cryptographyManager: CryptographyManager

    private val presenter by inject<IEnableBiometricLoginContract.Presenter> { parametersOf(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_biometric_login)
        presenter.init()
    }

    override fun setupView() {
        cancel?.setOnClickListener { finish() }

        authorize?.setOnClickListener {
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

    override fun showBiometricPromptForEncryption() {
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val secretKeyName = getString(R.string.secret_key_name)
            cryptographyManager = CryptographyManager()
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            val biometricPrompt =
                BiometricPromptUtils.createBiometricPrompt(this, ::encryptAndStoreServerToken)
            val promptInfo = BiometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    //region private
    private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
        authResult.cryptoObject?.cipher?.apply {
            SampleAppUser.fakeToken?.let { token ->
                val encryptedServerTokenWrapper = cryptographyManager.encryptData(token, this)
                cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                    encryptedServerTokenWrapper,
                    applicationContext,
                    SHARED_PREFS_FILENAME,
                    Context.MODE_PRIVATE,
                    CIPHERTEXT_WRAPPER
                )
            }
        }
        finish()
    }

    //endregion
}
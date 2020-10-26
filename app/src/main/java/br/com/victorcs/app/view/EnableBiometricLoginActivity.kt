package br.com.victorcs.app.view

import android.content.Context
import android.os.Bundle
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.widget.addTextChangedListener
import br.com.victorcs.app.R
import br.com.victorcs.app.SampleAppUser
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
        /*loginViewModel.loginResult.observe(this, Observer {
            val loginResult = it ?: return@Observer
            if (loginResult.success) {
                showBiometricPromptForEncryption()
            }
        })*/

    }

    override fun setupView() {
        cancel?.setOnClickListener { finish() }

        authorize?.setOnClickListener {
            presenter.validadeLogin(
                username?.text?.toString().orEmpty(),
                password?.text?.toString().orEmpty()
            )
        }

        username?.addTextChangedListener {text: Editable? ->
            presenter.validateUser(text?.toString().orEmpty())
        }

        password?.addTextChangedListener {text: Editable? ->
            presenter.validatePass(text?.toString().orEmpty())
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

    //region private
    private fun showBiometricPromptForEncryption() {
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

    private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
        authResult.cryptoObject?.cipher?.apply {
            SampleAppUser.fakeToken?.let { token ->
//                Log.d(TAG, "The token from server is $token")
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
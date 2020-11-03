package br.com.victorcs.app.view.enable

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import br.com.victorcs.app.R
import br.com.victorcs.app.utils.*
import br.com.victorcs.biometricauth.BiometricPromptUtils
import br.com.victorcs.biometricauth.IBiometricPrompt
import br.com.victorcs.biometricauth.data.repository.CryptographyManager
import br.com.victorcs.biometricauth.data.repository.ICryptographyManager
import kotlinx.android.synthetic.main.activity_enable_biometric_login.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class EnableBiometricLoginActivity : AppCompatActivity(), IEnableBiometricLoginContract.View {
    private lateinit var cryptographyManager: ICryptographyManager

    private val presenter by inject<IEnableBiometricLoginContract.Presenter> { parametersOf(this) }
    private val biometricPromptUtils by inject<IBiometricPrompt> { parametersOf(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_biometric_login)
        presenter.init()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            onBackPressed()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun setupView() {

        toolbar?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        cancel?.setOnClickListener { finish() }

        authorize?.setOnClickListener {
            submitAction()
        }

        username?.apply {
            requestFocus()
            showKeyboard(this)
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    presenter.validateUser(editable?.toString().orEmpty())
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
        }

        password?.apply{
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    presenter.validatePass(editable?.toString().orEmpty())
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submitAction()
                    return@OnEditorActionListener true
                }
                false
            })
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

    override fun showBiometricPromptForEncryption() {
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val secretKeyName = getString(R.string.secret_key_name)
            cryptographyManager = CryptographyManager()
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            val biometricPrompt =
                biometricPromptUtils.createBiometricPrompt(this, ::encryptAndStoreServerToken)
            val promptInfo = biometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    //region private
    private fun submitAction() {
        presenter.validateLogin(
            username?.text?.toString().orEmpty(),
            password?.text?.toString().orEmpty()
        )
        hideKeyboard(username)
    }

    private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
        authResult.cryptoObject?.cipher?.apply {
            SampleAppUser.fakeToken?.let { token ->
                val encryptedServerTokenWrapper = cryptographyManager.encryptData(token, this)
                cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                    encryptedServerTokenWrapper,
                    applicationContext,
                    SHARED_PREFS_FILENAME,
                    Context.MODE_PRIVATE,
                    CIPHER_TEXT_WRAPPER
                )
            }
        }
        finish()
    }

    //endregion
}
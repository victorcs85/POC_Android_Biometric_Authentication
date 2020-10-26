package br.com.victorcs.app.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import br.com.victorcs.app.R
import br.com.victorcs.app.utils.SampleAppUser
import br.com.victorcs.biometricauth.BiometricPromptUtils
import br.com.victorcs.biometricauth.CIPHERTEXT_WRAPPER
import br.com.victorcs.biometricauth.CryptographyManager
import br.com.victorcs.biometricauth.SHARED_PREFS_FILENAME
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

/**
 * 1) after entering "valid" username and password, login button becomes enabled
 * 2) User clicks biometrics?
 *   - a) if no template exists, then ask user to register template
 *   - b) if template exists, ask user to confirm by entering username & password
 */
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

       /* binding.useBiometrics.setOnClickListener {
            if (ciphertextWrapper != null) {
                showBiometricPromptForDecryption()
            } else {
                startActivity(Intent(this, EnableBiometricLoginActivity::class.java))
            }
        }
        if (ciphertextWrapper == null) {
            setupForLoginWithPassword()
        }*/
    }

    /**
     * The logic is kept inside onResume instead of onCreate so that authorizing biometrics takes
     * immediate effect.
     */
    override fun onResume() {
        super.onResume()

        if (ciphertextWrapper != null) {
            if (SampleAppUser.fakeToken == null) {
                showBiometricPromptForDecryption()
            } else {
                // The user has already logged in, so proceed to the rest of the app
                // this is a todo for you, the developer
                updateApp(getString(R.string.already_signedin))
            }
        }
    }

    override fun setupView() {
        login?.setOnClickListener {
            presenter.validadeLogin(
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

        use_biometrics?.setOnClickListener {
            if (ciphertextWrapper != null) {
                showBiometricPromptForDecryption()
            } else {
                startActivity(Intent(this, EnableBiometricLoginActivity::class.java))
            }
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

    // BIOMETRICS SECTION

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
                // Now that you have the token, you can query server for everything else
                // the only reason we call this fakeToken is because we didn't really get it from
                // the server. In your case, you will have gotten it from the server the first time
                // and therefore, it's a real token.

                updateApp(getString(R.string.already_signedin))
            }
        }
    }

    /*private fun setupForLoginWithPassword() {
        loginWithPasswordViewModel.loginWithPasswordFormState.observe(this, Observer { formState ->
            val loginState = formState ?: return@Observer
            when (loginState) {
                is SuccessfulLoginFormState -> binding.login.isEnabled = loginState.isDataValid
                is FailedLoginFormState -> {
                    loginState.usernameError?.let { binding.username.error = getString(it) }
                    loginState.passwordError?.let { binding.password.error = getString(it) }
                }
            }
        })
        loginWithPasswordViewModel.loginResult.observe(this, Observer {
            val loginResult = it ?: return@Observer
            if (loginResult.success) {
                updateApp(
                    "You successfully signed up using password as: user " +
                            "${SampleAppUser.username} with fake token ${SampleAppUser.fakeToken}"
                )
            }
        })
    }*/

    private fun updateApp(successMsg: String) {
        success?.text = successMsg
    }
}
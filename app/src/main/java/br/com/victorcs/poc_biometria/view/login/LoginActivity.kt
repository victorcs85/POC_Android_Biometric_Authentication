package br.com.victorcs.poc_biometria.view.login

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import br.com.victorcs.biometricauth.IBiometricPrompt
import br.com.victorcs.biometricauth.data.repository.ICryptographyManager
import br.com.victorcs.poc_biometria.R
import br.com.victorcs.poc_biometria.utils.*
import br.com.victorcs.poc_biometria.view.base.BaseActivity
import br.com.victorcs.poc_biometria.view.home.HomeActivity
import br.com.victorcs.poc_biometria.view.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_info.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class LoginActivity : BaseActivity(), ILoginContract.View {
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
            }
        }

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
            hideKeyboard(username)
            hideKeyboard(password)
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

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            callSettings()
            true
        }
        R.id.action_info -> {
            showBottomSheetInfo()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun showBiometricPromptForEncryption() {
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val secretKeyName = getString(R.string.secret_key_name)
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
            val biometricPrompt =
                biometricPromptUtils.createBiometricPrompt(this, ::encryptAndStoreServerToken)
            val promptInfo = biometricPromptUtils.createPromptInfo(this)
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    private fun showBottomSheetInfo() {
        // Modifica o comportamento do bottom sheet
        // Permite que o usuário possa deslizar ele até o topo da tela.

        // Modifica o comportamento do bottom sheet
        // Permite que o usuário possa deslizar ele até o topo da tela.
        val childLayoutParams = bottomSheet.layoutParams
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        childLayoutParams.height = displayMetrics.heightPixels

        bottomSheet.layoutParams = childLayoutParams

        tvBiometricInfo?.let {
            it.text = ""
            it.text = getInfoResult()
        }

        tvBiometricIdsInfo?.let {
            it.text = ""
            it.text = getInfoIds()
        }

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
                    callHome()
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

    private fun callHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
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

                callHome()
            }
        }
    }

    //test faceID - only on biometric 1.1.0-beta01, without work face id at 9.0
    /* private fun canAuthenticateWithStrongBiometrics(): Boolean {
         return BiometricManager.from(this)
             .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
     }*/
    //endregion
}
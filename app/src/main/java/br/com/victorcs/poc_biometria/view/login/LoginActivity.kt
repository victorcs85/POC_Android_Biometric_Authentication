package br.com.victorcs.poc_biometria.view.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import br.com.victorcs.biometricauth.IBiometricPrompt
import br.com.victorcs.biometricauth.data.repository.ICryptographyManager
import br.com.victorcs.poc_biometria.R
import br.com.victorcs.poc_biometria.utils.*
import br.com.victorcs.poc_biometria.view.base.BaseActivity
import br.com.victorcs.poc_biometria.view.home.HomeActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import javax.crypto.Cipher


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
    private val firebaseUtils by inject<IFirebaseUtils> { parametersOf(this) }
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

        /*val useFacial = SettingsUtils.getUseBiometricFacial(this)
        if (useFacial) {
            if(SampleAppUser.fakeToken == null) {
                showBiometricPromptForDecryption()
            } else {
                callHome()
            }
            return
        }*/
        if (cipherTextWrapper != null/* && !useFacial*/) {
            showBiometricPromptByFakeToken()
        }

    }

    override fun onBackPressed() {
        exitApp()
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
            authAction()
        }

        username?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                presenter.validateUser(editable?.toString().orEmpty())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        password?.let {
            it.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(editable: Editable?) {
                    presenter.validatePass(editable?.toString().orEmpty())
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
            it.setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        authAction()
                        false
                    }
                    else -> false
                }
            }
        }

        use_biometric_face?.let {
            use_biometric_face.isChecked = SettingsUtils.getUseBiometricFacial(this)
            it.setOnCheckedChangeListener { compoundButton, isChecked ->
                cryptographyManager.clear(
                    getString(R.string.secret_key_name),
                    applicationContext,
                    SHARED_PREFS_FILENAME,
                    Context.MODE_PRIVATE
                )
                SettingsUtils.updateUseBiometricFacial(this, isChecked)
                if(isChecked) {
                    showAlertInfo(getString(R.string.how_to_facial))
                }
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

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            callSettings()
            true
        }
        R.id.action_info -> {
            showAlertInfo(null)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showAlertInfo(message: String?) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle(getString(R.string.alert_title))
            if(message == null) {
                setMessage(
                    getInfoResult().plus("\n").plus(getInfoIds())
                )
            } else {
                setMessage(message)
            }
            setNeutralButton(getString(android.R.string.ok)) { _, _ -> }
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showBiometricPromptForDecryption() {
        cipherTextWrapper?.let { textWrapper ->
            val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
            firebaseUtils.logBiometricCanAuthenticateReturn(canAuthenticate)
            if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                val secretKeyName = getString(R.string.secret_key_name)
                val cipher = cryptographyManager.getInitializedCipherForDecryption(
                    secretKeyName, textWrapper.initializationVector, this@LoginActivity,
                    getString(R.string.secret_key_name),
                    Context.MODE_PRIVATE
                ) {
                    showToast(getString(R.string.new_finger))
                    return@getInitializedCipherForDecryption
                }
                if (cipher != null) {
                    biometricPrompt =
                        biometricPromptUtils.createBiometricPrompt(
                            this@LoginActivity,
                            processSuccess = ::decryptServerTokenFromStorage,
                            processError = ::logFirebaseErrorCode,
                            processFailed = {}
                        )
                    val useFacial = SettingsUtils.getUseBiometricFacial(this)
                    val promptInfo =
                        biometricPromptUtils.createPromptInfo(this@LoginActivity, useFacial)
                    try {
                        if (useFacial) {
                            biometricPrompt.authenticate(
                                promptInfo
                            ) //via facial
                        } else {
                            biometricPrompt.authenticate(
                                promptInfo,
                                BiometricPrompt.CryptoObject(cipher)
                            )
                        }
                    } catch (e: Exception) {
                        Timber.e(e.toString())
                        firebaseUtils.logNonFatalLog(e)
                        showToast(getString(R.string.error))
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
            firebaseUtils.logNonFatalLog(e)
            showToast(getString(R.string.error))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
    }

    private fun callHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun encryptAndStoreServerToken(authResult: BiometricPrompt.AuthenticationResult) {
        authResult.cryptoObject?.cipher?.apply {
            SampleAppUser.fakeToken?.let { token ->
                encryptFakeToken(token, this)
            }
        }
    }

    private fun authAction() {
        presenter.validateLogin(
            username?.text?.toString().orEmpty(),
            password?.text?.toString().orEmpty()
        ) { checkShowBiometricLogin() }
        hideKeyboard(username)
        hideKeyboard(password)
    }

    private fun encryptFakeToken(token: String, cipher: Cipher) {//TODO
        val encryptedServerTokenWrapper = cryptographyManager.encryptData(token, cipher)
        cryptographyManager.persistCiphertextWrapperToSharedPrefs(
            encryptedServerTokenWrapper,
            applicationContext,
            SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE,
            CIPHER_TEXT_WRAPPER
        )

        postDelay({ callHome() }, 500)
    }

    private fun checkShowBiometricLogin() {
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS &&
            SettingsUtils.getUseBiometricSettings(this@LoginActivity)
        ) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle(getString(R.string.alert_title))
                setMessage(getString(R.string.alert_description))
                setPositiveButton(getString(R.string.yes)) { _, _ -> showBiometricPrompt() }
                setNegativeButton(getString(R.string.no)) { _, _ -> callHome() }
            }
            val dialog: AlertDialog = builder.create()
            dialog.show()
        } else {
            callHome()
        }
    }

    private fun showBiometricPrompt() {
        val canAuthenticate = BiometricManager.from(applicationContext).canAuthenticate()
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val secretKeyName = getString(R.string.secret_key_name)
            val cipher = cryptographyManager.getInitializedCipherForEncryption(secretKeyName)
           /* biometricPromptUtils.createBiometricPrompt(this, processSuccess = {
                encryptFakeToken(
                    secretKeyName,
                    cipher
                )
            }, processFailed = {}, processError = ::logFirebaseErrorCode)*/
            val useFacial = SettingsUtils.getUseBiometricFacial(this)
            val promptInfo = biometricPromptUtils.createPromptInfo(this, useFacial)
            if (useFacial) {
                biometricPrompt =
                    biometricPromptUtils.createBiometricPrompt(
                        this,
                        processSuccess = {
                            showToast(getString(R.string.authenticated, SampleAppUser.fakeToken))
                        }, processFailed = {},
                        processError = ::logFirebaseErrorCode
                    )
                biometricPrompt.authenticate(promptInfo) //via facial
            } else {
                biometricPrompt =
                    biometricPromptUtils.createBiometricPrompt(
                        this,
                        processSuccess = ::encryptAndStoreServerToken, processFailed = {},
                        processError = ::logFirebaseErrorCode
                    )
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    private fun logFirebaseErrorCode(code: Int) {
        firebaseUtils.logNonFatalLog(code.toString())
    }

    private fun showBiometricPromptByFakeToken() {
        if (SampleAppUser.fakeToken == null) {
            showBiometricPromptForDecryption()
        }
    }

    //test faceID - only on biometric 1.1.0-beta01, without work face id at 9.0
    /* private fun canAuthenticateWithStrongBiometrics(): Boolean {
         return BiometricManager.from(this)
             .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
     }*/
    //endregion
}


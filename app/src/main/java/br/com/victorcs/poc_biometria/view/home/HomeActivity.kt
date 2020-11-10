package br.com.victorcs.poc_biometria.view.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import br.com.victorcs.biometricauth.IBiometricPrompt
import br.com.victorcs.biometricauth.data.repository.ICryptographyManager
import br.com.victorcs.poc_biometria.R
import br.com.victorcs.poc_biometria.utils.CIPHER_TEXT_WRAPPER
import br.com.victorcs.poc_biometria.utils.SHARED_PREFS_FILENAME
import br.com.victorcs.poc_biometria.utils.SampleAppUser
import br.com.victorcs.poc_biometria.view.base.BaseActivity
import br.com.victorcs.poc_biometria.view.login.LoginActivity
import kotlinx.android.synthetic.main.fragment_info.*
import kotlinx.android.synthetic.main.toolbar.*
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class HomeActivity : BaseActivity(), IHomeContract.View {

    private val presenter by inject<IHomeContract.Presenter> { parametersOf(this) }
    private val cryptographyManager by inject<ICryptographyManager> { parametersOf(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        presenter.init()
    }

    override fun onResume() {
        super.onResume()
        setupResults()
    }

    override fun onBackPressed() {
        exitApp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_exit -> {
            cryptographyManager.clear(getString(R.string.secret_key_name),
                applicationContext,
                SHARED_PREFS_FILENAME,
                Context.MODE_PRIVATE)
            callLogin()
            true
        }
        R.id.action_settings -> {
            callSettings()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun setupView() {

        toolbar?.let {
            setSupportActionBar(it)
        }

        setupResults()
    }

    //region private
    private fun callLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setupResults() {
        tvBiometricInfo?.let {
            it.text = ""
            it.text = getInfoResult().plus("\n").plus(getInfoIds())
        }

        tvBiometricIdsInfo?.let {
            it.text = ""
            it.text = getString(R.string.auth_result, SampleAppUser.fakeToken)
        }
    }
    //endregion
}
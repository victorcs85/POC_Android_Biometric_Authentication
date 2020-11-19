package br.com.victorcs.poc_biometria.di

import android.os.Build
import androidx.annotation.RequiresApi
import br.com.victorcs.poc_biometria.view.base.IValidate
import br.com.victorcs.poc_biometria.view.base.ValidatePresenter
import br.com.victorcs.poc_biometria.view.home.HomePresenter
import br.com.victorcs.poc_biometria.view.home.IHomeContract
import br.com.victorcs.poc_biometria.view.login.ILoginContract
import br.com.victorcs.poc_biometria.view.login.LoginPresenter
import br.com.victorcs.biometricauth.BiometricPromptUtils
import br.com.victorcs.biometricauth.IBiometricPrompt
import br.com.victorcs.biometricauth.data.repository.CryptographyManager
import br.com.victorcs.biometricauth.data.repository.ICryptographyManager
import br.com.victorcs.poc_biometria.utils.FirebaseUtils
import br.com.victorcs.poc_biometria.utils.IFirebaseUtils
import org.koin.dsl.module

object PresentationModule {

    @RequiresApi(Build.VERSION_CODES.N)
    val module = module {

        single<IValidate> {
            ValidatePresenter()
        }

        single<IFirebaseUtils> {
            FirebaseUtils()
        }

        single<ICryptographyManager> {
            CryptographyManager()
        }

        single<IBiometricPrompt> {
            BiometricPromptUtils()
        }

        factory<ILoginContract.Presenter> { (view: ILoginContract.View) ->
            LoginPresenter(
                view = view,
                validate = get()
            )
        }
        factory<IHomeContract.Presenter> { (view: IHomeContract.View) ->
            HomePresenter(
                view = view
            )
        }

    }
}
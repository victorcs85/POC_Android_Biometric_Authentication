package br.com.victorcs.app.di

import br.com.victorcs.app.view.base.IValidate
import br.com.victorcs.app.view.base.ValidatePresenter
import br.com.victorcs.app.view.enable.EnableBiometricLoginPresenter
import br.com.victorcs.app.view.enable.IEnableBiometricLoginContract
import br.com.victorcs.app.view.login.ILoginContract
import br.com.victorcs.app.view.login.LoginPresenter
import org.koin.dsl.module

object PresentationModule {

    val module = module {

        single<IValidate> {
            ValidatePresenter()
        }

        factory<ILoginContract.Presenter> { (view: ILoginContract.View) ->
            LoginPresenter(
                view = view,
                validate = get()
            )
        }
        factory<IEnableBiometricLoginContract.Presenter> { (view: IEnableBiometricLoginContract.View) ->
            EnableBiometricLoginPresenter(
                view = view,
                validate = get()
            )
        }

    }
}
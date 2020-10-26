package br.com.victorcs.app.di

import br.com.victorcs.app.view.EnableBiometricLoginPresenter
import br.com.victorcs.app.view.IEnableBiometricLoginContract
import br.com.victorcs.app.view.ILoginContract
import br.com.victorcs.app.view.LoginPresenter
import org.koin.dsl.module

object PresentationModule {

    val module = module {

        factory<ILoginContract.Presenter> { (view: ILoginContract.View) ->
            LoginPresenter(
                view = view
            )
        }
        factory<IEnableBiometricLoginContract.Presenter> { (view: IEnableBiometricLoginContract.View) ->
            EnableBiometricLoginPresenter(
                view = view
            )
        }

    }
}
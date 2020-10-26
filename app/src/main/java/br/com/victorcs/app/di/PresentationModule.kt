package br.com.victorcs.app.di

import br.com.victorcs.app.view.EnableBiometricLoginPresenter
import br.com.victorcs.app.view.IEnableBiometricLoginContract
import org.koin.dsl.module

object PresentationModule {

    val module = module {

        factory<IEnableBiometricLoginContract.Presenter> { (view: IEnableBiometricLoginContract.View) ->
            EnableBiometricLoginPresenter(
                view = view
            )
        }

    }
}
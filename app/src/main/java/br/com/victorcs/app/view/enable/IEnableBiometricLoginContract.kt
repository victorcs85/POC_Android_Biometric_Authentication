package br.com.victorcs.app.view.enable

import br.com.victorcs.app.view.base.IBaseView

object IEnableBiometricLoginContract {
    interface View: IBaseView {
        fun setupView()
        fun showBiometricPromptForEncryption()
    }

    interface Presenter {
        fun init()
        fun validateUser(userValue: String)
        fun validatePass(passValue: String)
        fun validateLogin(userValue: String, passValue: String)
    }
}
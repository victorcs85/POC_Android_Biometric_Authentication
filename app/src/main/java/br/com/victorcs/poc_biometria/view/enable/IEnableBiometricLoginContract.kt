package br.com.victorcs.poc_biometria.view.enable

import br.com.victorcs.poc_biometria.view.base.IBaseView

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
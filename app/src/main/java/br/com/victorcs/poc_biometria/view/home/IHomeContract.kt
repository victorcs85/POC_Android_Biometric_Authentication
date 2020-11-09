package br.com.victorcs.poc_biometria.view.home

object IHomeContract {
    interface View {
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
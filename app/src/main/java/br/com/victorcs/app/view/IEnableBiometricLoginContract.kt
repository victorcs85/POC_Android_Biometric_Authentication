package br.com.victorcs.app.view

object IEnableBiometricLoginContract {
    interface View {
        fun setupView()
        fun showUserError()
        fun showPassError()
        fun hideUserError()
        fun hidePassError()
        fun showBiometricPromptForEncryption()
    }

    interface Presenter {
        fun init()
        fun validateUser(userValue: String)
        fun validatePass(passValue: String)
        fun validadeLogin(userValue: String, passValue: String)
    }
}
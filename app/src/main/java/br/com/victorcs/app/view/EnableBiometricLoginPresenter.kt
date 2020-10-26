package br.com.victorcs.app.view

import br.com.victorcs.app.view.IEnableBiometricLoginContract

class EnableBiometricLoginPresenter(private val view: IEnableBiometricLoginContract.View)
    : IEnableBiometricLoginContract.Presenter {

    private var isValidLogin = false

    override fun init() {
        view.setupView()
    }

    override fun validateUser(userValue: String) {
        if(userValue.length < 11) {
            view.showUserError()
        } else {
            view.hideUserError()
        }
    }

    override fun validatePass(passValue: String) {
        if(passValue.length < 6) {
            view.showPassError()
        } else {
            view.hidePassError()
        }
    }

    override fun validadeLogin(userValue: String, passValue: String) {
        TODO("Not yet implemented")
    }
}
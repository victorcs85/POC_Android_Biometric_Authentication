package br.com.victorcs.poc_biometria.view.home

import br.com.victorcs.poc_biometria.view.base.IValidate

class HomePresenter(
    private val view: IHomeContract.View,
    private val validate: IValidate
) : IHomeContract.Presenter {

    private var isValidLogin = false

    override fun init() {
        view.setupView()
    }

    override fun validateUser(userValue: String) {
        if (validate.validateUser(userValue)) {
            view.hideUserError()
        } else {
            view.showUserError()
        }
    }

    override fun validatePass(passValue: String) {
        if (validate.validatePass(passValue)) {
            view.hidePassError()
        } else {
            view.showPassError()
        }
    }

    override fun validateLogin(userValue: String, passValue: String) {
        //TODO validate backend
        validate.doFakeLogin(userValue, passValue) { view.showBiometricPromptForEncryption() }
    }
}
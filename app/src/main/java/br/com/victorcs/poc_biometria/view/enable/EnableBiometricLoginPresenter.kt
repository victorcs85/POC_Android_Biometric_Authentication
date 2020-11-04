package br.com.victorcs.poc_biometria.view.enable

import br.com.victorcs.poc_biometria.view.base.IValidate

class EnableBiometricLoginPresenter(
    private val view: IEnableBiometricLoginContract.View,
    private val validate: IValidate
) : IEnableBiometricLoginContract.Presenter {

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
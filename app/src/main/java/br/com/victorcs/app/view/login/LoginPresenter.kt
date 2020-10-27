package br.com.victorcs.app.view.login

import br.com.victorcs.app.view.base.IValidate

class LoginPresenter(
    private val view: ILoginContract.View,
    private val validate: IValidate
) : ILoginContract.Presenter {

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
        validate.doFakeLogin(userValue, passValue, null)
    }
}
package br.com.victorcs.app.view.base

import br.com.victorcs.app.utils.SampleAppUser

class ValidatePresenter : IValidate {

    override fun validateUser(userValue: String) = userValue.length >= 11

    override fun validatePass(passValue: String) = passValue.length >= 6

    override fun doFakeLogin(userValue: String, passValue: String, action: (() -> Unit)?) {
        if (validateUser(userValue) && validatePass(passValue)) {
            SampleAppUser.username = userValue
            SampleAppUser.fakeToken = java.util.UUID.randomUUID().toString()
            action?.invoke()
        }
    }
}
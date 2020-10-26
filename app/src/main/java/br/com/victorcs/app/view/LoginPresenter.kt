package br.com.victorcs.app.view

class LoginPresenter(private val view: ILoginContract.View)
    : ILoginContract.Presenter {

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
package br.com.victorcs.app.view

object ILoginContract {
    interface View {
        fun setupView()
        fun showUserError()
        fun showPassError()
        fun hideUserError()
        fun hidePassError()
    }

    interface Presenter {
        fun init()
        fun validateUser(userValue: String)
        fun validatePass(passValue: String)
        fun validadeLogin(userValue: String, passValue: String)
    }
}
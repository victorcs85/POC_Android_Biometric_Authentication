package br.com.victorcs.app.view.login

import br.com.victorcs.app.view.base.IBaseView

object ILoginContract {
    interface View: IBaseView {
        fun setupView()
    }

    interface Presenter {
        fun init()
        fun validateUser(userValue: String)
        fun validatePass(passValue: String)
        fun validateLogin(userValue: String, passValue: String)
    }
}
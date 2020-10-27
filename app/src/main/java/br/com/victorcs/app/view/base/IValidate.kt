package br.com.victorcs.app.view.base

interface IValidate {
    fun validateUser(userValue: String): Boolean
    fun validatePass(passValue: String): Boolean
    fun doFakeLogin(userValue: String, passValue: String, action: (() -> Unit)?)
}
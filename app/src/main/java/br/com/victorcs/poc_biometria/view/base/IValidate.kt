package br.com.victorcs.poc_biometria.view.base

interface IValidate {
    fun validateUser(userValue: String): Boolean
    fun validatePass(passValue: String): Boolean
    fun doFakeLogin(userValue: String, passValue: String, action: (() -> Unit)?)
}
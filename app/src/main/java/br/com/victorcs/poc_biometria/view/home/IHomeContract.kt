package br.com.victorcs.poc_biometria.view.home

object IHomeContract {
    interface View {
        fun setupView()
    }

    interface Presenter {
        fun init()
    }
}
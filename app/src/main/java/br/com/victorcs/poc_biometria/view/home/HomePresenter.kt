package br.com.victorcs.poc_biometria.view.home

class HomePresenter(
    private val view: IHomeContract.View
) : IHomeContract.Presenter {

    override fun init() {
        view.setupView()
    }

}
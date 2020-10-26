package br.com.victorcs.app

import android.app.Application
import br.com.victorcs.app.di.PresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(PresentationModule.module)
        }
    }
}
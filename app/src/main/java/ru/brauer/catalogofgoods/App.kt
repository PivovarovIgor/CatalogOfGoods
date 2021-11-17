package ru.brauer.catalogofgoods

import android.app.Application
import ru.brauer.catalogofgoods.di.AppModule
import ru.brauer.catalogofgoods.di.DaggerAppComponent

class App : Application() {

    companion object {
        lateinit var instance: App
    }

    val appComponent by lazy {
        DaggerAppComponent.builder()
            .appModule(AppModule(this@App))
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
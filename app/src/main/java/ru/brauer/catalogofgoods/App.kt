package ru.brauer.catalogofgoods

import android.app.Application
import ru.brauer.catalogofgoods.di.DaggerAppComponent

class App : Application() {

    companion object {
        lateinit var instance: App
    }

    val appComponent by lazy {
        DaggerAppComponent.builder()
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
package ru.brauer.catalogofgoods

import android.app.Application
import android.util.Log
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import ru.brauer.catalogofgoods.di.AppComponent
import ru.brauer.catalogofgoods.di.DaggerAppComponent
import java.io.IOException
import java.net.SocketException


class App : Application() {

    companion object {
        lateinit var instance: App
    }

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
            .context(this@App)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        RxJavaPlugins.setErrorHandler { exception: Throwable ->
            var _exeption = exception
            if (_exeption is UndeliverableException) {
                _exeption = _exeption.cause!!
            }
            if (_exeption is IOException || _exeption is SocketException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }
            if (_exeption is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler
            }
            if (_exeption is NullPointerException || _exeption is IllegalArgumentException) {
                // that's likely a bug in the application
                Thread.currentThread()
                    .uncaughtExceptionHandler?.uncaughtException(Thread.currentThread(), _exeption)
                return@setErrorHandler
            }
            if (_exeption is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler
                    ?.uncaughtException(Thread.currentThread(), _exeption)
                return@setErrorHandler
            }
            Log.w("Undeliverable exception received, not sure what to do", _exeption)
        }
    }
}
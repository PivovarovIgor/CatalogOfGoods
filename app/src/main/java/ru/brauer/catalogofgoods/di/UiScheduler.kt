package ru.brauer.catalogofgoods.di

import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import javax.inject.Singleton

@Module
class UiScheduler {

    @Singleton
    @Provides
    fun uiScheduler(): Scheduler = AndroidSchedulers.mainThread()
}
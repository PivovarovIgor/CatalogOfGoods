package ru.brauer.catalogofgoods.di

import dagger.Module
import dagger.Provides
import io.reactivex.rxjava3.disposables.CompositeDisposable
import ru.brauer.catalogofgoods.rx.ISchedulerProvider
import ru.brauer.catalogofgoods.rx.SchedulerProvider
import javax.inject.Singleton

@Module
class RxModule {

    @Provides
    fun compositeDisposable(): CompositeDisposable = CompositeDisposable()

    @Singleton
    @Provides
    fun schedulersProvider(): ISchedulerProvider = SchedulerProvider()
}
package ru.brauer.catalogofgoods.stubs

import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.brauer.catalogofgoods.rx.ISchedulerProvider

class SchedulerProviderStub : ISchedulerProvider {
    override fun ui(): Scheduler = Schedulers.trampoline()

    override fun io(): Scheduler = Schedulers.trampoline()
}

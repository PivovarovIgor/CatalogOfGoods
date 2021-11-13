package ru.brauer.catalogofgoods.data

import android.annotation.SuppressLint
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.brauer.catalogofgoods.domain.IRepository

class MockRepository : IRepository {
    @SuppressLint("AuthLeak")
    override fun getGoods(): Single<List<Goods>> = Single.fromCallable {
        Thread.sleep(2000L)
        listOf(
            Goods(
                "Фартук",
                "https://blondypro.ru/upload/iblock/f48/f48036173043a663915d9893a128e497.png"
            ),
            Goods("Рубашка", ""),
            Goods("Штаны", ""),
        )
    }.subscribeOn(Schedulers.io())
}
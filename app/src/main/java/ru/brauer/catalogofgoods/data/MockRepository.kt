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
                "ftp://fakelftp:poiPOI098@195.133.242.197/webdata/000000003/goods/1/import_files/03/030e5efa-369c-11e9-80e5-005056912b25_4db8a542-92a2-11e9-80f6-005056912b25.jpg"
            ),
            Goods("Рубашка", ""),
            Goods("Штаны", ""),
        )
    }.subscribeOn(Schedulers.io())
}
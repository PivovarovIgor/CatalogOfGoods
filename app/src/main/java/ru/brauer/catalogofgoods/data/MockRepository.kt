package ru.brauer.catalogofgoods.data

import android.annotation.SuppressLint
import ru.brauer.catalogofgoods.domain.IRepository

class MockRepository : IRepository {
    @SuppressLint("AuthLeak")
    override fun getGoods(): List<Goods> =
        listOf(
            Goods("Фартук", "ftp://fakelftp:poiPOI098@195.133.242.197/webdata/000000003/goods/1/import_files/03/030e5efa-369c-11e9-80e5-005056912b25_4db8a542-92a2-11e9-80f6-005056912b25.jpg"),
            Goods("Рубашка", ""),
            Goods("Штаны", ""),
        )
}
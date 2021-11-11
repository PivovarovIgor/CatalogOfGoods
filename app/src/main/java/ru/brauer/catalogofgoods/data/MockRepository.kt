package ru.brauer.catalogofgoods.data

import ru.brauer.catalogofgoods.domain.IRepository

class MockRepository : IRepository {
    override fun getGoods(): List<Goods> =
        listOf(
            Goods("Фартук", ""),
            Goods("Рубашка", ""),
            Goods("Штаны", ""),
        )
}
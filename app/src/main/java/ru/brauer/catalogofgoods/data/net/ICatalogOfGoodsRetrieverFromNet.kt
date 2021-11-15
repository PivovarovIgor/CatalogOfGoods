package ru.brauer.catalogofgoods.data.net

import ru.brauer.catalogofgoods.data.entities.Goods

interface ICatalogOfGoodsRetrieverFromNet {
    fun retrieve(): List<Goods>
}
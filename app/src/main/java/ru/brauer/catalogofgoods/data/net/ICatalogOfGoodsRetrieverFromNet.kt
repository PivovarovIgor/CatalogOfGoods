package ru.brauer.catalogofgoods.data.net

import ru.brauer.catalogofgoods.data.Goods

interface ICatalogOfGoodsRetrieverFromNet {
    fun retrieve(): List<Goods>
}
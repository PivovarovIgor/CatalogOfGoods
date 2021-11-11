package ru.brauer.catalogofgoods.domain

import ru.brauer.catalogofgoods.data.Goods

interface IRepository {
    fun getGoods(): List<Goods>
}
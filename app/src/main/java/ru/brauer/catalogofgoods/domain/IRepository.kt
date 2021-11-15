package ru.brauer.catalogofgoods.domain

import io.reactivex.rxjava3.core.Single
import ru.brauer.catalogofgoods.data.entities.Goods

interface IRepository {
    fun getGoods(): Single<List<Goods>>
}
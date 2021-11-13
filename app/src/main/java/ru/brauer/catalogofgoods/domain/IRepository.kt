package ru.brauer.catalogofgoods.domain

import io.reactivex.rxjava3.core.Single
import ru.brauer.catalogofgoods.data.Goods

interface IRepository {
    fun getGoods(): Single<List<Goods>>
}
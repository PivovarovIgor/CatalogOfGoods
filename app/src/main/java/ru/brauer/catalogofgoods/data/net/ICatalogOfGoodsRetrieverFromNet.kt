package ru.brauer.catalogofgoods.data.net

import io.reactivex.rxjava3.core.Observable
import ru.brauer.catalogofgoods.data.commerceml.EntityOfCommerceMl
import ru.brauer.catalogofgoods.data.entities.Goods


interface ICatalogOfGoodsRetrieverFromNet {
    fun retrieve(): Observable<List<EntityOfCommerceMl>>
}
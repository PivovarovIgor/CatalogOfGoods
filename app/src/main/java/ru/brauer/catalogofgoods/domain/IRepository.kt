package ru.brauer.catalogofgoods.domain

import androidx.paging.PagingSource
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import ru.brauer.catalogofgoods.data.database.entities.GoodsEnt
import ru.brauer.catalogofgoods.data.entities.Goods

interface IRepository {
    fun getGoods(processingLoadingObserver: Observer<BackgroundLoadingState.LoadingState>): Single<List<Goods>>
    fun getPagingSource(): PagingSource<Int, GoodsEnt>
}
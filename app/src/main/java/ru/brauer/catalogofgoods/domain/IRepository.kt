package ru.brauer.catalogofgoods.domain

import androidx.paging.rxjava3.RxPagingSource
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import ru.brauer.catalogofgoods.data.entities.Goods

interface IRepository {
    fun getGoods(processingLoadingObserver: Observer<BackgroundLoadingState.LoadingState>): Single<List<Goods>>
    fun getPagingSource(): RxPagingSource<Int, Goods>
}
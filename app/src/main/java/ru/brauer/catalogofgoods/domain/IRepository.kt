package ru.brauer.catalogofgoods.domain

import androidx.paging.PagingData
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.Flow
import ru.brauer.catalogofgoods.data.entities.Goods

interface IRepository {
    fun getGoods(processingLoadingObserver: Observer<BackgroundLoadingState.LoadingState>): Single<List<Goods>>
    fun getPagingFlowFromLocalSource(): Flow<PagingData<Goods>>
}
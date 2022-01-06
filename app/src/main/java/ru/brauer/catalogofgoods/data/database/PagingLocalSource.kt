package ru.brauer.catalogofgoods.data.database

import androidx.paging.PagingState
import androidx.paging.rxjava3.RxPagingSource
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.data.toBusinessData
import javax.inject.Inject

class PagingLocalSource @Inject constructor(
    private val appDatabase: AppDatabase
) : RxPagingSource<Int, Goods>() {

    override fun loadSingle(params: LoadParams<Int>): Single<LoadResult<Int, Goods>> {
        val nextPage = params.key ?: 0
        val loadSize = params.loadSize
        return appDatabase.goodsDao.getPage(loadSize, nextPage * loadSize)
            .subscribeOn(Schedulers.io())
            .map { goodsEnt ->
                LoadResult.Page(
                    goodsEnt.toBusinessData(),
                    if (nextPage == 0) null else nextPage - 1,
                    if (goodsEnt.size < loadSize) null else nextPage + 1
                ) as LoadResult<Int, Goods>
            }
            .onErrorReturn { LoadResult.Error(it) }
    }

    override fun getRefreshKey(state: PagingState<Int, Goods>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }
}
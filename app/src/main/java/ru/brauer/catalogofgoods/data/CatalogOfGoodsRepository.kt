package ru.brauer.catalogofgoods.data

import android.util.Log
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import ru.brauer.catalogofgoods.data.database.AppDatabase
import ru.brauer.catalogofgoods.data.database.GoodsEnt
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.data.net.ICatalogOfGoodsRetrieverFromNet
import ru.brauer.catalogofgoods.domain.IRepository
import javax.inject.Inject

class CatalogOfGoodsRepository @Inject constructor(
    private val catalogOfGoodsRetriever: ICatalogOfGoodsRetrieverFromNet,
    private val appDatabase: AppDatabase
) :
    IRepository {

    private var disposable: Disposable? = null

    override fun getGoods(processingLoadingObserver: Observer<Boolean>): Single<List<Goods>> =
        Single.fromCallable {
            val processingSubject : Subject<Boolean> = BehaviorSubject.create()
            processingSubject.onNext(true)
            processingSubject.subscribe(processingLoadingObserver)
            if (disposable?.isDisposed == false) {
                disposable?.dispose()
                Log.i("CatalogOfGoodsRepository", "loading is disposed")
            }
            catalogOfGoodsRetriever
                .retrieve()
                .observeOn(Schedulers.io())
                .subscribe({
                    Log.i("goods", "Before getting on database " + Thread.currentThread().id)
                    appDatabase.goodsDao.insert(it.toDatabaseData())
                    Log.i("goods", "After getting on database " + Thread.currentThread().id)
                }, {
                    processingSubject.onError(it)
                    throw it
                }, {
                    processingSubject.onComplete()
                }).also { disposable = it }
            appDatabase.goodsDao.getAll().toBusinessData()
        }.subscribeOn(Schedulers.io())
}

fun GoodsEnt.toBusinessData(): Goods =
    Goods(
        id = id,
        name = name,
        photoUrl = photoUrl
    )

fun Goods.toDatabaseData(): GoodsEnt =
    GoodsEnt(
        id = id,
        name = name,
        photoUrl = photoUrl
    )

fun List<GoodsEnt>.toBusinessData(): List<Goods> = map { it.toBusinessData() }

fun List<Goods>.toDatabaseData(): List<GoodsEnt> = map { it.toDatabaseData() }
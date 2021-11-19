package ru.brauer.catalogofgoods.data

import android.util.Log
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import ru.brauer.catalogofgoods.data.commerceml.EntityOfCommerceMl
import ru.brauer.catalogofgoods.data.database.AppDatabase
import ru.brauer.catalogofgoods.data.database.entities.GoodsEnt
import ru.brauer.catalogofgoods.data.database.entities.OfferEnt
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
            val processingSubject: Subject<Boolean> = BehaviorSubject.create()
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
                    appDatabase.goodsDao.insert(it.toDatabaseDataListOfGoods())
                    appDatabase.offerDao.insert(it.toDatabaseDataListOfOffer())
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

fun EntityOfCommerceMl.Goods.toDatabaseData(): GoodsEnt =
    GoodsEnt(
        id = id,
        name = name,
        photoUrl = photoUrl.firstOrNull() ?: ""
    )

fun EntityOfCommerceMl.Offer.toDatabaseData(): OfferEnt? {

    if (name.isBlank()) {
        return null
    }

    val separatedId: List<String> = id.split('#', ignoreCase = false, limit = 2)
        .filter { it.isNotBlank() }

    return OfferEnt(
        id = separatedId.last(),
        name = name,
        goodsId = separatedId.first()
    )
}

fun List<GoodsEnt>.toBusinessData(): List<Goods> = map { it.toBusinessData() }

fun List<EntityOfCommerceMl>.toDatabaseDataListOfGoods(): List<GoodsEnt> = mapNotNull {
    (it as? EntityOfCommerceMl.Goods)?.toDatabaseData()
}

fun List<EntityOfCommerceMl>.toDatabaseDataListOfOffer(): List<OfferEnt> = mapNotNull {
    (it as? EntityOfCommerceMl.Offer)?.toDatabaseData()
}
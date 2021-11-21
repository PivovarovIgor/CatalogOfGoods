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
import ru.brauer.catalogofgoods.data.database.entities.PhotoOfGoodsEnt
import ru.brauer.catalogofgoods.data.database.entities.PriceEnt
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.data.net.ICatalogOfGoodsRetrieverFromNet
import ru.brauer.catalogofgoods.domain.BackgroundLoadingState
import ru.brauer.catalogofgoods.domain.IRepository
import java.math.BigDecimal
import javax.inject.Inject

class CatalogOfGoodsRepository @Inject constructor(
    private val catalogOfGoodsRetriever: ICatalogOfGoodsRetrieverFromNet,
    private val appDatabase: AppDatabase
) :
    IRepository {

    private var disposable: Disposable? = null

    override fun getGoods(processingLoadingObserver: Observer<BackgroundLoadingState.LoadingState>): Single<List<Goods>> =
        Single.fromCallable {
            val processingSubject: Subject<BackgroundLoadingState.LoadingState> =
                BehaviorSubject.create()
            var count = 0
            processingSubject.onNext(BackgroundLoadingState.LoadingState(count))
            processingSubject.subscribe(processingLoadingObserver)
            if (disposable?.isDisposed == false) {
                disposable?.dispose()
                Log.i("CatalogOfGoodsRepository", "loading is disposed")
            }
            catalogOfGoodsRetriever
                .retrieve()
                .observeOn(Schedulers.io())
                .subscribe({
                    it.toDatabaseDataListOfGoods()
                        .also { entities ->
                            count += entities.count()
                            if (entities.isNotEmpty()) {
                                appDatabase.goodsDao.insert(entities)
                            }
                        }
                    it.toDatabaseDataListOfPhotos()
                        .also { entities ->
                            count += entities.count()
                            if (entities.isNotEmpty()) {
                                appDatabase.photoOfGoodsDao.insert(entities)
                            }
                        }
                    it.toDatabaseDataListOfOffer()
                        .also { entities ->
                            count += entities.count()
                            if (entities.isNotEmpty()) {
                                appDatabase.offerDao.insert(entities)
                            }
                        }
                    it.toDatabaseDataListOfPrices()
                        .also { entities ->
                            count += entities.count()
                            if (entities.isNotEmpty()) {
                                appDatabase.priceDao.insert(entities)
                            }
                        }
                    processingSubject.onNext(BackgroundLoadingState.LoadingState(count))
                }, {
                    processingSubject.onError(it)
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

fun EntityOfCommerceMl.Goods.getPhotosOfGoodsToDatabaseData(): List<PhotoOfGoodsEnt> =
    photoUrl.map {
        PhotoOfGoodsEnt(
            photoUrl = it,
            goodsId = this.id
        )
    }

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

fun EntityOfCommerceMl.Offer.toGetPricesToDatabaseData(): List<PriceEnt> =
    prices.map { price ->
        PriceEnt(
            offerId = this.id,
            presentation = price.name,
            typePriceId = price.typePriceId,
            priceValue = price.price
                .toBigDecimal()
                .multiply(BigDecimal.valueOf(100))
                .toInt(),
            currency = price.currency
        )
    }


fun List<GoodsEnt>.toBusinessData(): List<Goods> = map { it.toBusinessData() }

fun List<EntityOfCommerceMl>.toDatabaseDataListOfGoods(): List<GoodsEnt> = mapNotNull {
    (it as? EntityOfCommerceMl.Goods)?.toDatabaseData()
}

fun List<EntityOfCommerceMl>.toDatabaseDataListOfPhotos(): List<PhotoOfGoodsEnt> = mapNotNull {
    (it as? EntityOfCommerceMl.Goods)?.getPhotosOfGoodsToDatabaseData()
}.flatten()

fun List<EntityOfCommerceMl>.toDatabaseDataListOfOffer(): List<OfferEnt> = mapNotNull {
    (it as? EntityOfCommerceMl.Offer)?.toDatabaseData()
}

fun List<EntityOfCommerceMl>.toDatabaseDataListOfPrices(): List<PriceEnt> = mapNotNull {
    (it as? EntityOfCommerceMl.Offer)?.toGetPricesToDatabaseData()
}.flatten()
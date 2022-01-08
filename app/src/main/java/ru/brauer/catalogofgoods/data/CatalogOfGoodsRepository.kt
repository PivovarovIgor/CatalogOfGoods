package ru.brauer.catalogofgoods.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.brauer.catalogofgoods.data.commerceml.EntityOfCommerceMl
import ru.brauer.catalogofgoods.data.database.AppDatabase
import ru.brauer.catalogofgoods.data.database.entities.*
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.data.entities.Offer
import ru.brauer.catalogofgoods.data.entities.Price
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
            }
            catalogOfGoodsRetriever
                .retrieve()
                .observeOn(Schedulers.io())
                .doOnSubscribe { appDatabase.startUpdatingData() }
                .doOnComplete { appDatabase.deleteNotUpdatedData() }
                .doOnError { appDatabase.endUpdatingData() }
                .doOnDispose { appDatabase.endUpdatingData() }
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
                    it.toDatabaseDataListOfRests()
                        .also { entities ->
                            count += entities.count()
                            if (entities.isNotEmpty()) {
                                appDatabase.restDao.insert(entities)
                            }
                        }
                    processingSubject.onNext(BackgroundLoadingState.LoadingState(count))
                }, {
                    processingSubject.onError(it)
                }, {
                    processingSubject.onComplete()
                }).also { disposable = it }
            appDatabase.goodsDao.getAll().toBusinessData(appDatabase)
        }.subscribeOn(Schedulers.io())

    override fun getPagingFlowFromLocalSource(): Flow<PagingData<Goods>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                maxSize = MAX_SIZE_CACHING_OF_PAGING,
            ),
            pagingSourceFactory = { appDatabase.goodsDao.getPage() }
        ).flow
            .map { pagingData ->
                pagingData.map {
                    withContext(Dispatchers.IO) {
                        it.toBusinessData(appDatabase)
                    }
                }
            }

    companion object {
        private const val PAGE_SIZE = 30
        private const val MAX_SIZE_CACHING_OF_PAGING = PAGE_SIZE * 3
    }
}

const val MAIN_PRICE_TYPE = "fdf5831f-8b8c-11e9-80f4-005056912b25"

fun GoodsEnt.toBusinessData(appDataBase: AppDatabase): Goods =
    Goods(
        id = id,
        name = name,
        listOfPhotosUri = appDataBase.photoOfGoodsDao
            .getPhotosByGoodsId(id)
            .toBusinessData(),
        offers = appDataBase.offerDao
            .getOffersByGoodsId(id)
            .toBusinessData(appDataBase)
    )

private fun List<OfferEnt>.toBusinessData(appDataBase: AppDatabase): List<Offer> {
    val listOfRests = appDataBase.restDao
        .getRestsByOffersId(this.map { it.id })
    val listOfPrices = appDataBase.priceDao
        .getPricesByOffersId(this.map { it.id }, MAIN_PRICE_TYPE)
    return this.map { it.toBusinessData(listOfRests, listOfPrices) }
}

private fun OfferEnt.toBusinessData(listOfRests: List<RestEnt>, listOfPrices: List<PriceEnt>) =
    Offer(
        id = this.id,
        name = this.name,
        stock = listOfRests
            .filter { it.offerId == this.id }
            .sumOf { it.count },
        price = (listOfPrices
            .let { listOfPricesEnt ->
                listOfPricesEnt.find { it.offerId == this.id }
                    ?: PriceEnt.empty()
            }
            .toBusinessData())
    )

private fun PriceEnt.toBusinessData(): Price =
    Price(
        presentation = this.presentation,
        priceValue = this.priceValue,
        currency = this.currency
    )

@JvmName("toBusinessDataPhotoOfGoodsEnt")
private fun List<PhotoOfGoodsEnt>.toBusinessData(): List<String> =
    this.map { it.photoUrl }

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
                .toBigDecimalOrNull()
                ?.multiply(BigDecimal.valueOf(100))
                ?.toInt() ?: 0,
            currency = price.currency
        )
    }

fun EntityOfCommerceMl.Offer.toGetRestsToDatabaseData(): List<RestEnt> =
    rests.map { rest ->
        RestEnt(
            offerId = this.id,
            count = rest.count.toIntOrNull() ?: 0
        )
    }

@JvmName("toBusinessDataGoodsEnt")
fun List<GoodsEnt>.toBusinessData(appDatabase: AppDatabase): List<Goods> =
    map { it.toBusinessData(appDatabase) }

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

fun List<EntityOfCommerceMl>.toDatabaseDataListOfRests(): List<RestEnt> = mapNotNull {
    (it as? EntityOfCommerceMl.Offer)?.toGetRestsToDatabaseData()
}.flatten()
package ru.brauer.catalogofgoods.data

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.paging.*
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
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
import ru.brauer.catalogofgoods.rx.ISchedulerProvider
import java.math.BigDecimal
import javax.inject.Inject

class CatalogOfGoodsRepository @Inject constructor(
    private val catalogOfGoodsRetriever: ICatalogOfGoodsRetrieverFromNet,
    private val appDatabase: AppDatabase,
    private val schedulersProvider: ISchedulerProvider
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
                .observeOn(schedulersProvider.io())
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
            listOf<Goods>() // TODO
        }.subscribeOn(schedulersProvider.io())

    override fun getPagingFlowFromLocalSource(filter: (GoodsEnt) -> List<Pair<Int, Int>>?): Flow<PagingData<Goods>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                maxSize = PAGE_SIZE * 5
            ),
            pagingSourceFactory = { appDatabase.goodsDao.getPage() }
        ).flow
            .map { pagingData ->
                pagingData
                    .filter {
                        filter(it)?.isNotEmpty() ?: true
                    }
                    .map {
                        withContext(Dispatchers.IO) {
                            it.toBusinessData(appDatabase, filter)
                        }
                    }
            }

    override fun disposeObservables() {
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
        disposable = null
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}

const val MAIN_PRICE_TYPE = "fdf5831f-8b8c-11e9-80f4-005056912b25"

fun GoodsEnt.toBusinessData(
    appDataBase: AppDatabase,
    filter: (GoodsEnt) -> List<Pair<Int, Int>>?
): Goods {

    val pricesEntOfOffer: Map<OfferEnt, List<PriceEnt>> =
        appDataBase.offerDao.getOffersAndPricesByGoodsId(id, MAIN_PRICE_TYPE)
    val restsEntOfOffer: Map<OfferEnt, List<RestEnt>> =
        appDataBase.offerDao.getOffersAndRestsByGoodsId(id)
    val flatPricesEntOfOffer: List<PriceEnt> = pricesEntOfOffer.flatMap { it.value }
    val offers: List<OfferEnt> = (restsEntOfOffer.keys + pricesEntOfOffer.keys)
        .toMutableList()
        .sortedBy { it.name }

    val foundSubstrings = filter(this)
    val spannedString = foundSubstrings?.let { toSpanning ->
        SpannableString(name).apply {
            toSpanning.forEach {
                setSpan(
                    ForegroundColorSpan(Color.BLUE),
                    it.first,
                    it.second,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
                setSpan(
                    BackgroundColorSpan(0xffc9ff00.toInt()),
                    it.first,
                    it.second,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
                setSpan(
                    StyleSpan(Typeface.BOLD),
                    it.first,
                    it.second,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
            }
        }
    }

    return Goods(
        id = id,
        name = spannedString ?: name,
        listOfPhotosUri = appDataBase.photoOfGoodsDao
            .getPhotosByGoodsId(id)
            .toBusinessData(),
        offers = offers
            .toBusinessData(id, pricesEntOfOffer, restsEntOfOffer),
        maxPricePresent = flatPricesEntOfOffer
            .filter { it.priceValue != 0 }
            .maxOfOrNull { it.priceValue }
            ?.let { maxPriceValue: Int ->
                flatPricesEntOfOffer.find { it.priceValue == maxPriceValue }?.presentation
            }
            ?: Goods.EMPTY_PRICE,
        stock = restsEntOfOffer
            .flatMap { it.value }
            .sumOf { it.count }
    )
}

private fun List<OfferEnt>.toBusinessData(
    goodsId: String,
    listOfPrices: Map<OfferEnt, List<PriceEnt>>,
    listOfRests: Map<OfferEnt, List<RestEnt>>
): List<Offer> = map { it.toBusinessData(goodsId, listOfRests, listOfPrices) }

private fun OfferEnt.toBusinessData(
    goodsId: String,
    listOfRests: Map<OfferEnt, List<RestEnt>>,
    listOfPrices: Map<OfferEnt, List<PriceEnt>>
): Offer {
    val offerId = if (goodsId != this.id) {
        "$goodsId#"
    } else {
        ""
    } + this.id

    return Offer(
        id = offerId,
        name = this.name,
        stock = listOfRests[this]
            ?.sumOf { it.count }
            ?: 0,
        price = (listOfPrices
            .let { listOfPricesEnt: Map<OfferEnt, List<PriceEnt>> ->
                listOfPricesEnt[this]
                    ?.firstOrNull()
                    ?: PriceEnt.empty()
            }
            .toBusinessData())
    )
}

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
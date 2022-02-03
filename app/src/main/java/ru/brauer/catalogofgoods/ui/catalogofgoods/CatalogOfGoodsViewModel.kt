package ru.brauer.catalogofgoods.ui.catalogofgoods

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.domain.AppState
import ru.brauer.catalogofgoods.domain.BackgroundLoadingState
import ru.brauer.catalogofgoods.domain.IRepository
import ru.brauer.catalogofgoods.rx.ISchedulerProvider
import javax.inject.Inject


class CatalogOfGoodsViewModel @Inject constructor(
    private val repository: IRepository,
    private val compositeDisposable: CompositeDisposable,
    private val schedulerProvider: ISchedulerProvider
) :
    ViewModel() {

    private val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData(
        AppState.Success(
            listOf()
        )
    )
    private val backgroundProcessing: MutableLiveData<BackgroundLoadingState> =
        MutableLiveData(BackgroundLoadingState.Complete)
    private var disposable: Disposable? = null
    var searchQueryText: String = ""
        private set

    private val filter: (goods: Goods) -> Boolean = { goods ->
        searchQueryText.isBlank() || goods.name.contains(searchQueryText, ignoreCase = true)
    }
    val dataPagingFlow = repository.getPagingFlowFromLocalSource(filter)
        .cachedIn(viewModelScope)

    private val processingLoadingObserver = object : Observer<BackgroundLoadingState.LoadingState> {

        private var processingDisposable: Disposable? = null

        override fun onSubscribe(disposableOnSubscribe: Disposable) {
            if (processingDisposable?.isDisposed == false) {
                processingDisposable?.dispose()
            }
            processingDisposable = disposableOnSubscribe
        }

        override fun onNext(processing: BackgroundLoadingState.LoadingState) {
            backgroundProcessing.postValue(processing)
        }

        override fun onError(exeption: Throwable) {
            backgroundProcessing.postValue(BackgroundLoadingState.Error(exeption))
        }

        override fun onComplete() {
            backgroundProcessing.postValue(BackgroundLoadingState.Complete)
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQueryText = query
        liveDataToObserve.postValue(AppState.upDateOnSearch(query))
    }

    fun observe(
        lifecycleOwner: LifecycleOwner,
        renderData: (AppState) -> Unit,
        renderBackgroundProcessing: ((backgroundLoadingState: BackgroundLoadingState) -> Unit)? = null
    ) {
        liveDataToObserve.observe(lifecycleOwner, renderData)
        renderBackgroundProcessing?.let {
            backgroundProcessing.observe(lifecycleOwner, it)
        }
    }

    fun beginLoadingData() {
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
        disposable = getData()
    }

    private fun getData(): Disposable {
        liveDataToObserve.postValue(AppState.Loading)
        return repository
            .getGoods(processingLoadingObserver)
            .observeOn(schedulerProvider.ui())
            .doOnDispose {
                liveDataToObserve.postValue(AppState.Success(listOf()))
            }
            .subscribe({
                liveDataToObserve.postValue(AppState.Success(it))
            }, {
                liveDataToObserve.postValue(AppState.Error(it))
            }).also { compositeDisposable.add(it) }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
        repository.disposeObservables()
    }
}
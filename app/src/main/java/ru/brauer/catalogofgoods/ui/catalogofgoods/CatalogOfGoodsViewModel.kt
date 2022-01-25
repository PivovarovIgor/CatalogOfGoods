package ru.brauer.catalogofgoods.ui.catalogofgoods

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.flow.MutableStateFlow
import ru.brauer.catalogofgoods.data.entities.Goods
import ru.brauer.catalogofgoods.domain.AppState
import ru.brauer.catalogofgoods.domain.BackgroundLoadingState
import ru.brauer.catalogofgoods.domain.IRepository
import javax.inject.Inject


class CatalogOfGoodsViewModel @Inject constructor(
    private val repository: IRepository,
    private val uiScheduler: Scheduler
) :
    ViewModel() {

    private val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData()
    private val backgroundProcessing: MutableLiveData<BackgroundLoadingState> = MutableLiveData()
    private var disposable: Disposable? = null
    private var searchQuery: String = ""

    private val filter: (goods: Goods) -> Boolean = { goods ->
        searchQuery.isBlank() || goods.name.contains(searchQuery, ignoreCase = true)
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
        searchQuery = query
        liveDataToObserve.postValue(AppState.upDateOnSearch(query))
    }

    fun observe(
        lifecycleOwner: LifecycleOwner,
        renderData: (AppState) -> Unit,
        renderBackgroundProcessing: ((backgroundLoadingState: BackgroundLoadingState) -> Unit)? = null
    ) {
        liveDataToObserve.observe(lifecycleOwner, renderData)
        if (liveDataToObserve.value !is AppState.Success
            && (disposable?.isDisposed != false)
        ) {
            disposable = getData()
        }
        renderBackgroundProcessing?.let {
            backgroundProcessing.observe(lifecycleOwner, it)
        }
    }

    private fun getData(): Disposable {
        liveDataToObserve.postValue(AppState.Loading)
        return repository
            .getGoods(processingLoadingObserver)
            .observeOn(uiScheduler)
            .doOnDispose {
                liveDataToObserve.postValue(AppState.Success(listOf()))
            }
            .subscribe({
                liveDataToObserve.postValue(AppState.Success(it))
            }, {
                liveDataToObserve.postValue(AppState.Error(it))
            })
    }
}
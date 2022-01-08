package ru.brauer.catalogofgoods.ui.catalogofgoods

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.flow.map
import ru.brauer.catalogofgoods.data.toBusinessData
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

    val dataPagingFlow = Pager(
        config = PagingConfig(
            pageSize = 6,
            maxSize = 24
        ),
        pagingSourceFactory = { repository.getPagingSource() }
    ).flow
        .map { pagingData ->
            pagingData.map {
                it.toBusinessData()
            }
        }
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
        liveDataToObserve.value = AppState.Loading
        return repository
            .getGoods(processingLoadingObserver)
            .observeOn(uiScheduler)
            .doOnDispose {
                liveDataToObserve.value = AppState.Success(listOf())
            }
            .subscribe({
                liveDataToObserve.value = AppState.Success(it)
            }, {
                liveDataToObserve.value = AppState.Error(it)
            })
    }
}
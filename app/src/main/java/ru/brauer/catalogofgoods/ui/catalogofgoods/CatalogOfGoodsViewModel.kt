package ru.brauer.catalogofgoods.ui.catalogofgoods

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.Disposable
import ru.brauer.catalogofgoods.domain.AppState
import ru.brauer.catalogofgoods.domain.IRepository
import javax.inject.Inject


class CatalogOfGoodsViewModel @Inject constructor(
    private val repository: IRepository,
    private val uiScheduler: Scheduler
) :
    ViewModel() {

    private val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData()
    private val backgroundProcessing: MutableLiveData<Boolean> = MutableLiveData()
    private var disposable: Disposable? = null

    private val processingLoadingObserver = object : Observer<Boolean> {

        private var processingDisposable: Disposable? = null

        override fun onSubscribe(disposableOnSubscribe: Disposable) {
            if (processingDisposable?.isDisposed == false) {
                processingDisposable?.dispose()
            }
            processingDisposable = disposableOnSubscribe
        }

        override fun onNext(processing: Boolean) {
            backgroundProcessing.postValue(processing)
        }

        override fun onError(exeption: Throwable) {
            backgroundProcessing.postValue(false)
        }

        override fun onComplete() {
            backgroundProcessing.postValue(false)
        }
    }

    init {
        backgroundProcessing.value = false
    }

    fun observe(
        lifecycleOwner: LifecycleOwner,
        renderData: (AppState) -> Unit,
        renderBackgroundProcessing: ((Boolean) -> Unit)? = null
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

    fun getItemCount() = (liveDataToObserve.value as? AppState.Success)?.listOfGoods?.count() ?: 0

    fun getDataAtPosition(position: Int) =
        (liveDataToObserve.value as? AppState.Success)
            ?.let { it.listOfGoods.elementAtOrNull(position) }

    private fun getData(): Disposable {
        liveDataToObserve.value = AppState.Loading
        return repository
            .getGoods(processingLoadingObserver)
            .observeOn(uiScheduler)
            .subscribe({
                liveDataToObserve.value = AppState.Success(it)
            }, {
                liveDataToObserve.value = AppState.Error(it)
            })
    }
}
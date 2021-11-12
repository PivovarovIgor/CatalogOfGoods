package ru.brauer.catalogofgoods.ui.catalogofgoods

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.brauer.catalogofgoods.domain.AppState
import ru.brauer.catalogofgoods.domain.IRepository
import javax.inject.Inject


class CatalogOfGoodsViewModel @Inject constructor(private val repository: IRepository) :
    ViewModel() {

    private val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData()

    fun observe(lifecycleOwner: LifecycleOwner, renderData: (AppState) -> Unit) {
        liveDataToObserve.observe(lifecycleOwner, renderData)
        if (liveDataToObserve.value !is AppState.Success) {
            Thread {
                getData()
            }.start()
        }
    }

    fun getItemCount() = (liveDataToObserve.value as? AppState.Success)?.listData?.count() ?: 0

    fun getDataAtPosition(position: Int) =
        (liveDataToObserve.value as? AppState.Success)
            ?.let { it.listData.elementAtOrNull(position) }

    private fun getData() {
        liveDataToObserve.postValue(AppState.Loading)
        Thread.sleep(2000L)
        liveDataToObserve.postValue(AppState.Success(repository.getGoods()))
    }
}
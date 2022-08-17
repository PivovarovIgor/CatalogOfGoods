package ru.brauer.catalogofgoods.ui.catalogofgoods

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import ru.brauer.catalogofgoods.data.database.entities.GoodsEnt
import ru.brauer.catalogofgoods.domain.AppState
import ru.brauer.catalogofgoods.domain.BackgroundLoadingState
import ru.brauer.catalogofgoods.domain.IRepository
import ru.brauer.catalogofgoods.extensions.getAllContains
import ru.brauer.catalogofgoods.services.BackgroundLoadingStateChannel
import javax.inject.Inject


class CatalogOfGoodsViewModel @Inject constructor(
    private val repository: IRepository,
    private val compositeDisposable: CompositeDisposable,
    backgroundLoadingStateChannel: BackgroundLoadingStateChannel,
) :
    ViewModel() {

    private val liveDataToObserve: MutableLiveData<AppState> = MutableLiveData(
        AppState.Success(
            listOf()
        )
    )

    var searchQueryText: String = ""
        private set

    private val filter: (GoodsEnt) -> List<Pair<Int, Int>>? = { goods ->
        if (searchQueryText.isBlank()) {
            null
        } else {
            goods.name.getAllContains(searchQueryText)
        }
    }

    val dataPagingFlow = repository.getPagingFlowFromLocalSource(filter)
        .cachedIn(viewModelScope)

    val backgroundLoadingState: StateFlow<BackgroundLoadingState> =
        backgroundLoadingStateChannel.backgroundLoadingState

    fun onSearchQueryChanged(query: String) {
        searchQueryText = query
        liveDataToObserve.postValue(AppState.upDateOnSearch(query))
    }

    fun observe(
        lifecycleOwner: LifecycleOwner,
        renderData: (AppState) -> Unit,
    ) {
        liveDataToObserve.observe(lifecycleOwner, renderData)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
        repository.disposeObservables()
    }
}
package ru.brauer.catalogofgoods.ui.detailsofgoods

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import ru.brauer.catalogofgoods.data.entities.Goods

class DetailsOfGoodsViewModel(
    val goods: Goods
) : ViewModel() {
    @Suppress("UNCHECKED_CAST")
    class Factory @AssistedInject constructor(@Assisted("goods") private val goods: Goods) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetailsOfGoodsViewModel(goods) as T
        }

        @AssistedFactory
        interface Factory {
            fun create(@Assisted("goods") goods: Goods): DetailsOfGoodsViewModel.Factory
        }
    }
}
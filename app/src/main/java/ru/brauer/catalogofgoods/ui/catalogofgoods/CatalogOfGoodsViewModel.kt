package ru.brauer.catalogofgoods.ui.catalogofgoods

import androidx.lifecycle.ViewModel
import ru.brauer.catalogofgoods.domain.IRepository
import javax.inject.Inject


class CatalogOfGoodsViewModel @Inject constructor(private val repository: IRepository) :
    ViewModel() {
    val name = "hello"
}
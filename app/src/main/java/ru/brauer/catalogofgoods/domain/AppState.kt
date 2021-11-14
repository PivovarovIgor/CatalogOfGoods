package ru.brauer.catalogofgoods.domain

import ru.brauer.catalogofgoods.data.Goods

sealed class AppState {

    data class Success(val listOfGoods: List<Goods>) : AppState()

    class Error(val exception: Throwable) : AppState()

    object Loading : AppState()

}

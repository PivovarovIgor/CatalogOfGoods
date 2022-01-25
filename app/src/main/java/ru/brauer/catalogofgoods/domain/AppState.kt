package ru.brauer.catalogofgoods.domain

import ru.brauer.catalogofgoods.data.entities.Goods

sealed class AppState {

    data class Success(val listOfGoods: List<Goods>) : AppState()

    data class upDateOnSearch(val query: String): AppState()

    class Error(val exception: Throwable) : AppState()

    object Loading : AppState()
}
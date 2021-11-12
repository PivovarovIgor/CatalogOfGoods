package ru.brauer.catalogofgoods.domain

import ru.brauer.catalogofgoods.data.Goods

sealed class AppState {

    data class Success(val listData: List<Goods>) : AppState()

    class Error(val ex: Throwable) : AppState()

    object Loading : AppState()

}

package ru.brauer.catalogofgoods.domain

sealed class BackgroundLoadingState {

    data class LoadingState(val count: Int) : BackgroundLoadingState()

    class Error(val exception: Throwable) : BackgroundLoadingState()

    object Complete : BackgroundLoadingState()

}
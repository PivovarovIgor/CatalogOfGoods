package ru.brauer.catalogofgoods.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import ru.brauer.catalogofgoods.domain.BackgroundLoadingState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundLoadingStateChannel @Inject constructor() {

    private val channel: Channel<BackgroundLoadingState> = Channel(Channel.CONFLATED)
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun trySend(state: BackgroundLoadingState) {
        channel.trySend(state)
    }

    val backgroundLoadingState: StateFlow<BackgroundLoadingState> = flow {
        for (state in channel) {
            emit(state)
            delay(300L)
        }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = BackgroundLoadingState.Complete
    )
}
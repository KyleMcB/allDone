package com.xingpeds.alldone.shared.logic

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PairingState {
    object Loading : PairingState()
    object NotPaired : PairingState()
    data class Paired(val serverAddress: Url) : PairingState()
}

class ClientApplication(
    val settings: PersistedSettings,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    // get saved settings
    // what do I do if they are null?
    private val _serverAddress = MutableStateFlow<PairingState>(PairingState.Loading)
    val server: StateFlow<PairingState> get() = _serverAddress
    val scope = CoroutineScope(dispatcher)

    init {
        scope.launch {
            loadSettings()
        }
    }


    suspend fun loadSettings() {
        val serverAddress = settings.get("serverAddress")
        val serverPort = settings.get("serverPort")
        if (serverAddress != null && serverPort != null) {
            val pair = PairingState.Paired(Url("ws://$serverAddress:$serverPort/ws"))
            _serverAddress.value = pair
        } else {
            _serverAddress.emit(PairingState.NotPaired)
        }
    }

}
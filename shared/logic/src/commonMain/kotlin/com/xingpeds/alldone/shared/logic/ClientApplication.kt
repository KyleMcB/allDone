package com.xingpeds.alldone.shared.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


interface ConnectionToServerFun : suspend (Url, CoroutineScope) -> AttemptedServerConnection

sealed class AttemptedServerConnection {
    data class Success(val connection: ServerConnection) : AttemptedServerConnection()
    sealed class Failure : AttemptedServerConnection() {
        object ConnectionRefused : Failure()
    }
}

interface ServerConnection {

    val serverAddress: Url
    suspend fun send(message: ClientMessage)
    val incoming: Flow<ServerMessage>
}


const val stateKey = "stateKey"
const val deviceKey = "deviceKey"

class ClientApplication(
    val settings: PersistedSettings,
    val connectionToServer: ConnectionToServerFun,
    val navigator: Navigator,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val appScope: CoroutineScope = CoroutineScope(dispatcher),
) {
    private val device = MutableStateFlow<Device?>(null)
    private val clientStateFlow = MutableStateFlow<ClientState>(ClientState.Loading)
    val stateFlow: StateFlow<ClientState> = clientStateFlow

    fun start(): Job = appScope.launch {
        val stateString = settings.get(stateKey)
        clientStateFlow.emit(stateString?.toClientState() ?: ClientState.TabulaRasa)
        val deviceString = settings.get(deviceKey)
        device.emit(deviceString?.toDeviceOrCreateNew() ?: Device(randUuid()))
        clientStateFlow.onEach(::saveState).launchIn(appScope)
        device.onEach(::saveDevice).launchIn(appScope)
    }

    private suspend fun saveState(clientState: ClientState) {
        settings.set(stateKey, Json.encodeToString(clientState))
    }

    private suspend fun saveDevice(device: Device?) {
        device ?: return
        settings.set(deviceKey, Json.encodeToString(device))
    }

    suspend fun getDevice(): Device {
        return device.filterNotNull().first()
    }

}

@Serializable
sealed class ClientState {
    @Serializable
    object Loading : ClientState()

    @Serializable
    object TabulaRasa : ClientState()
}

internal fun String.toClientState(): ClientState {
    return Json.decodeFromString<ClientState>(this)
}

internal fun String.toDeviceOrCreateNew(): Device {
    return Json.decodeFromString<Device>(this)
}


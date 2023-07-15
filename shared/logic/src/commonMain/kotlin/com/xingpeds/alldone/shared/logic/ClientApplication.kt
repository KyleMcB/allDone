package com.xingpeds.alldone.shared.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

sealed class PairingState {
    object Loading : PairingState()
    object NotPaired : PairingState()
    data class Paired(val serverAddress: Url) : PairingState()
}

fun interface ConnectionToServerFun : (Url) -> AttemptedServerConnection

sealed class AttemptedServerConnection {
    data class Success(val connection: ServerConnection) : AttemptedServerConnection()
    sealed class Failure : AttemptedServerConnection() {
        object ConnectionRefused : Failure()
    }
}

interface ServerConnection {

    suspend fun send(message: ClientMessage)
    val incoming: Flow<ServerMessage>
}

const val stateKey = "stateKey"

class ClientApplication(
    val settings: PersistedSettings,
    val connectionToServer: ConnectionToServerFun,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val navigator: Navigator,
) {
    val appScope = CoroutineScope(dispatcher)
    val clientState = MutableStateFlow(ClientState.Loading as ClientState)

    init {
        appScope.launch {
            loadState()
        }
        clientState.onEach(::handleClientState).launchIn(appScope)
    }

    private suspend fun handleClientState(state: ClientState) {
        when (state) {
            ClientState.Loading -> Unit
            is ClientState.Paired -> TODO()
            is ClientState.PendingUser -> TODO()
            ClientState.TabulaRasa -> navigator.navigateTo(Screen.TabulaRasa)
        }
    }

    private suspend fun loadState() {
        val state = settings.get(stateKey)
        if (state == null) {
            clientState.emit(ClientState.TabulaRasa)
        }
    }
}

@Serializable
sealed class ClientState {
    @Serializable
    object Loading : ClientState()

    @Serializable
    object TabulaRasa : ClientState()

    @Serializable
    data class PendingUser(val userData: UserData) : ClientState()

    @Serializable
    data class Paired(val user: User, val device: Device, val serverAddress: Url) : ClientState()

}

sealed class ConnectionState {
    object Loading : ConnectionState()
    object ConnectionRefused : ConnectionState()

    data class Connected(val connection: ServerConnection) : ConnectionState()
}

@Serializable
sealed class UserState {
    @Serializable
    object Loading : UserState()

    @Serializable
    object NoUser : UserState()

    @Serializable
    data class NewUserPending(val userData: UserData) : UserState()

    @Serializable
    data class UserLoaded(val user: User, val device: Device) : UserState()
}

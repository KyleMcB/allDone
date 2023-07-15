package com.xingpeds.alldone.shared.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

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

const val UserKey = "user"

class ClientApplication(
    val settings: PersistedSettings,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val connectionToServer: ConnectionToServerFun,
) {
    // get saved settings
    // what do I do if they are null?
    private val _serverAddress = MutableStateFlow<PairingState>(PairingState.Loading)
    val server: StateFlow<PairingState> get() = _serverAddress
    val scope = CoroutineScope(dispatcher)
    val _savedUser = MutableStateFlow<UserState>(UserState.Loading)
    val savedUser: StateFlow<UserState> get() = _savedUser
    val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Loading)
    val connectionState: StateFlow<ConnectionState> get() = _connectionState

    init {
        scope.launch {
            loadSettings()
        }
        server.onEach(::observeServerState).launchIn(scope)
        connectionState.combine(savedUser) { connectionState, user ->
            if (connectionState is ConnectionState.Connected && user is UserState.UserLoaded) {
                login(user.user, connectionState.connection)
            }
        }
    }


    private suspend fun observeServerState(serverState: PairingState) {
        when (serverState) {
            is PairingState.Paired -> {
                when (val connection = connectionToServer(serverState.serverAddress)) {
                    is AttemptedServerConnection.Success -> {
                        _connectionState.emit(ConnectionState.Connected(connection.connection))
                    }

                    is AttemptedServerConnection.Failure.ConnectionRefused -> {
                        _connectionState.emit(ConnectionState.ConnectionRefused)
                    }
                }
            }

            PairingState.Loading -> TODO()
            PairingState.NotPaired -> askUserToPairToServer()
        }
    }

    private suspend fun login(user: User, server: ServerConnection) {
        val device = Device(randUuid())
        server.send(IdentifyUser(user, device))
    }

    private fun askUserToPairToServer() {
        // show dialog
        // get user input
        // save settings
        // load settings
    }


    /**
     * idempotent
     * safe to call multiple times
     */
    suspend fun loadSettings() {
        val serverAddress = settings.get("serverAddress")
        val serverPort = settings.get("serverPort")
        if (serverAddress != null && serverPort != null) {
            val pair = PairingState.Paired(Url("ws://$serverAddress:$serverPort/ws"))
            _serverAddress.emit(pair)
        } else {
            _serverAddress.emit(PairingState.NotPaired)
        }
        val userJson = settings.get(UserKey)
        if (userJson != null) {
            val user = Json.decodeFromString<User>(userJson)
            _savedUser.emit(UserState.UserLoaded(user))
        } else {
            _savedUser.emit(UserState.NoUser)
        }
    }

}

sealed class ConnectionState {
    object Loading : ConnectionState()
    object ConnectionRefused : ConnectionState()

    data class Connected(val connection: ServerConnection) : ConnectionState()
}

sealed class UserState {
    object Loading : UserState()
    object NoUser : UserState()
    data class UserLoaded(val user: User) : UserState()
}

package com.xingpeds.alldone.shared.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
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

val defaultExceptionHandler = CoroutineExceptionHandler { context, exception ->
    println("context: $context")
    println("exception: $exception")
}


const val stateKey = "stateKey"
const val deviceKey = "deviceKey"

class ClientApplication(
    val settings: PersistedSettings,
    val connectionToServer: ConnectionToServerFun,
    val userInput: UserInputManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val exceptionHandler: CoroutineExceptionHandler = defaultExceptionHandler,
    val appScope: CoroutineScope = CoroutineScope(dispatcher + exceptionHandler),
    autoStart: Boolean = true,
) {
    init {
        if (autoStart) {
            start()
        }
    }

    private val device = MutableStateFlow<Device?>(null)
    private val clientStateFlow = MutableStateFlow<ClientState>(ClientState.Loading)
    private val serverConnection = MutableStateFlow<ServerConnection?>(null)
    val stateFlow: StateFlow<ClientState> = clientStateFlow

    internal fun start(): Job = appScope.launch {
        val stateString = settings.get(stateKey)
        val deviceString = settings.get(deviceKey)
        clientStateFlow.emit(stateString?.toClientState() ?: ClientState.TabulaRasa)
        device.emit(deviceString?.toDeviceOrCreateNew() ?: Device(randUuid()))
        serverConnection.onEach { println("serverConnection: $it") }.launchIn(appScope)
        device.onEach(::saveDevice).launchIn(appScope)
        clientStateFlow
            .onEach { println("second printer $it") }
            .onEach(::saveState)
            .onEach(::handleClientState)
            .launchIn(appScope)

    }

    suspend fun getServerConnection(): ServerConnection {
        return serverConnection.filterNotNull().first()
    }

    @OptIn(FlowPreview::class)
    private suspend fun handleClientState(clientState: ClientState) {
        when (clientState) {
            ClientState.Loading -> Unit
            ClientState.TabulaRasa -> {
                val userDetailsAndServerUrl = userInput.getUserDetailsAndServerUrl()
                clientStateFlow.emit(
                    ClientState.AttemptingToRegister(
                        userDetailsAndServerUrl,
                        0
                    )
                )
            }

            is ClientState.AttemptingToRegister -> registerUser(clientState.userDetailsAndServerUrl)
            is ClientState.Paired -> {
                val connection =
                    serverConnection.first()
                if (connection == null) {
                    // we are starting up in a paired state and need to connect
                    connectionAndIdentify(clientState)
                } else {
                    // already have a connection
                    Unit
                }
            }
        }
    }

    private suspend fun connectionAndIdentify(clientState: ClientState.Paired) {
        val (_, serverUrl) = clientState
        val connectionScope = CoroutineScope(dispatcher + defaultExceptionHandler)
        when (val attempt = connectionToServer(serverUrl, connectionScope)) {
            AttemptedServerConnection.Failure.ConnectionRefused -> TODO()
            is AttemptedServerConnection.Success -> {
                val connection = attempt.connection
                connectionScope.launch {
                    connection.incoming.cancellable().collect {
                        when (it) {
                            is IdentifySuccess -> {
                                serverConnection.emit(connection)
                                cancel()
                            }

                            else -> Unit
                        }
                    }
                }
                connection.send(IdentifyUser(clientState.user, getDevice()))
            }
        }
    }

    //this function does a little too much, but it's fine for now
    private suspend fun registerUser(userDetailsAndServerUrl: NewUserAndServer) {
        val (userDetails, serverUrl) = userDetailsAndServerUrl
        val device = device.filterNotNull().first()
        val connectionScope = CoroutineScope(dispatcher)
        when (val attempt = connectionToServer(serverUrl, connectionScope)) {
            is AttemptedServerConnection.Success -> {
                val connection = attempt.connection
                connectionScope.launch {
                    connection.incoming.cancellable().collect {
                        when (it) {
                            is NewUserResponse -> {
                                serverConnection.emit(connection)
                                clientStateFlow.emit(ClientState.Paired(it.user, serverUrl))
                                cancel()
                            }

                            else -> Unit
                        }
                    }
                }
                connection.send(NewUserRequest(userDetails, device))
            }

            is AttemptedServerConnection.Failure -> Unit
        }
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

    @Serializable
    data class AttemptingToRegister(
        val userDetailsAndServerUrl: NewUserAndServer,
        val attempt: Int,
    ) : ClientState()

    @Serializable
    data class Paired(val user: User, val serverUrl: Url) : ClientState()
}

internal fun String.toClientState(): ClientState {
    return Json.decodeFromString<ClientState>(this)
}

internal fun String.toDeviceOrCreateNew(): Device {
    return Json.decodeFromString<Device>(this)
}


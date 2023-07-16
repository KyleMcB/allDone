package com.xingpeds.alldone.shared.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

sealed class PairingState {
    object Loading : PairingState()
    object NotPaired : PairingState()
    data class Paired(val serverAddress: Url) : PairingState()
}

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
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val navigator: Navigator,
) {
}


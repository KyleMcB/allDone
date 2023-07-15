package com.xingpeds.alldone.shared.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

const val UserKey = "user"
const val DeviceKey = "device"

class ClientApplication(
    val settings: PersistedSettings,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val connectionToServer: ConnectionToServerFun,
) {
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

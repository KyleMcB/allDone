package com.example.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class Authenticator(val data: Persistence) {

}

suspend fun Authenticator.login(
    connection: ClientConnection,
): User? = coroutineScope {
    val state = MutableStateFlow<User?>(null)
    launch { // we launch here so the cancel call only stops the work in this function
        connection.inbound.cancellable().take(3).collect { request ->
            when (request) {
                is PreAuthClientMessage ->
                    when (request) {
                        is NewUserRequest -> {
                            state.emit(addUser(request))
                            cancel()
                        }
                    }

                else -> connection.send(IdentifyFirst)
            }
        }
    }.join()
    state.value
}

suspend fun Authenticator.addUser(request: NewUserRequest): User {
    return data.addUser(request.userData)!! //todo: handle null
}
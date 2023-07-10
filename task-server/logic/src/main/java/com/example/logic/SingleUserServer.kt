package com.example.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * This is a server that handles a single user.
 * It will connect multiple clients to the same user.
 */
class SingleUserServer(
    val user: User,
    private val data: Persistence,
    private val context: CoroutineContext = Dispatchers.Default
) {
    private val scope = CoroutineScope(context)
    private val _connections: MutableStateFlow<Set<ClientConnection>> =
        MutableStateFlow(emptySet())
    val connections: StateFlow<Set<ClientConnection>> = _connections


    fun addConnection(connection: ClientConnection) = _connections.update { it + connection }

    fun broadcast(message: ServerMessage) = scope.launch {
        connections.value.forEach { it.send(message) }
    }


}
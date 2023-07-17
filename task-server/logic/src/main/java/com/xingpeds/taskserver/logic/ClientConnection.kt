package com.xingpeds.taskserver.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.flow.Flow

interface ClientConnection {
    val inbound: Flow<ClientMessage>

    suspend fun send(message: ServerMessage)

}

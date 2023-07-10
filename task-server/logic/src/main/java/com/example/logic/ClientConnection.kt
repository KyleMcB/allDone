package com.example.logic

import com.xingpeds.alldone.entities.*

interface ClientConnection {
    suspend fun send(message: ServerMessage)

}

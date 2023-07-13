package com.xingpeds

import com.xingpeds.alldone.entities.*
import com.xingpeds.plugins.configureSerialization
import com.xingpeds.plugins.configureSockets
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        val testScope = CoroutineScope(Job())
        val client = createClient {
            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }
        application {
            configureSerialization()
            configureSockets(testScope.coroutineContext)
        }
        client.webSocket("/ws") {
            println("connected")
            val request: ClientMessage = NewUserRequest(UserData("testuser"))
            println("sending")
            sendSerialized(request)
            println("receiving")
            println(receiveDeserialized<ServerMessage>())
            println("done")
            testScope.cancel()
        }
    }
}

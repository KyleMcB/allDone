package com.xingpeds.plugins

import com.example.logic.Authenticator
import com.example.logic.ClientConnection
import com.example.logic.MemoryNonPersistence
import com.example.logic.Persistence
import com.example.logic.SingleUserServer
import com.example.logic.login
import com.xingpeds.alldone.entities.*
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Duration
import kotlin.coroutines.CoroutineContext

typealias UserId = UUID

fun Application.configureSockets(serverContext: CoroutineContext = Dispatchers.Default) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    val data: Persistence = MemoryNonPersistence()
    val serverMap = mutableMapOf<UserId, SingleUserServer>()
    routing {
        webSocket("/ws") { // websocketSession
            val connectionScope = CoroutineScope(this.coroutineContext)
            val authenticator = Authenticator(data)
            val connection = WebSocketClientConnection(this, connectionScope)
            val user: User = authenticator.login(connection) ?: return@webSocket
            val server: SingleUserServer =
                serverMap.getOrPut(user.id) { SingleUserServer(user, data) }
            server.addConnection(connection)
            // I need a suspending function that prevents this from ending
            println("end")
        }
    }
}

class WebSocketClientConnection(
    private val socket: WebSocketServerSession,
    private val scope: CoroutineScope,
) : ClientConnection {
    private val bus = MutableSharedFlow<ClientMessage>()

    init {
        scope.launch {
            bus.emitAll(socket.incoming.receiveAsFlow()
                .filterIsInstance<Frame.Text>().map {
                    val string = it.readText()
                    println("WebSocketClientConnection: received $string")
                    Json.decodeFromString<ClientMessage>(string)
                })
        }
    }


    override val inbound: Flow<ClientMessage>
        get() = bus

    override suspend fun send(message: ServerMessage) {
        println("WebSocketClientConnection: sending $message")
        socket.sendSerialized(message)
    }

}
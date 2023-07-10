import com.example.logic.ClientConnection
import com.xingpeds.alldone.entities.*
import com.xingpeds.alldone.entities.test.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


val clientConnectionArb: Arb<ClientConnection> = arbitrary {
    object : ClientConnection {
        override val inbound: Flow<ClientMessage>
            get() = flowOf()

        override suspend fun send(message: ServerMessage) {
            TODO("Not yet implemented")
        }

    }
}

val serverMessageArb: Arb<ServerMessage> = arbitrary {
    NewUserResponse(userArb.bind())

}


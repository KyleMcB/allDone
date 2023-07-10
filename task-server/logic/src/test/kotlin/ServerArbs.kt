import com.example.logic.ClientConnection
import com.xingpeds.alldone.entities.*
import com.xingpeds.alldone.entities.test.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary


val clientConnectionArb: Arb<ClientConnection> = arbitrary {
    object : ClientConnection {
        override suspend fun send(message: ServerMessage) {
            TODO("Not yet implemented")
        }

    }
}

val serverMessageArb: Arb<ServerMessage> = arbitrary {
    NewUserResponse(userArb.bind())

}


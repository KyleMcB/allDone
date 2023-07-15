import com.xingpeds.alldone.entities.*
import com.xingpeds.alldone.entities.test.*
import com.xingpeds.alldone.shared.logic.AttemptedServerConnection
import com.xingpeds.alldone.shared.logic.ClientApplication
import com.xingpeds.alldone.shared.logic.ConnectionToServerFun
import com.xingpeds.alldone.shared.logic.PairingState
import com.xingpeds.alldone.shared.logic.PersistedSettings
import com.xingpeds.alldone.shared.logic.ServerConnection
import com.xingpeds.alldone.shared.logic.Url
import com.xingpeds.alldone.shared.logic.UserKey
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class TestServerConnection(
    val fromServerFlow: Flow<ServerMessage> = flowOf(),
    val onSendToServer: suspend (ClientMessage) -> Unit = {},
) : ServerConnection {
    override suspend fun send(message: ClientMessage) = onSendToServer(message)

    override val incoming: Flow<ServerMessage>
        get() = fromServerFlow
}

class ClientApplicationTestTDD {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun TestScope.getTestSubject(
        map: Map<String, String> = mapOf(),
        connectToServer: ConnectionToServerFun,
    ): ClientApplication {
        val clientApplication = ClientApplication(
            object : PersistedSettings {
                override suspend fun get(key: String): String? {
                    return map[key]
                }
            },
            dispatcher = UnconfinedTestDispatcher(scheduler = this.testScheduler),
            connectionToServer = connectToServer
        )
        backgroundScope.launch {
            clientApplication.server.collect {
                println(it)
            }
        }
        return clientApplication
    }

    // can not test the loading state because it loads too quickly and the stateflow skips it initial state


    @Test
    fun `application loads settings`() = runTest {
        val subject =
            getTestSubject(mapOf("serverAddress" to "localhost", "serverPort" to "8080")) {
                AttemptedServerConnection.Success(TestServerConnection())
            }
        subject.loadSettings()
        subject.server.value shouldBe PairingState.Paired(Url("ws://localhost:8080/ws"))
    }

    @Test
    fun `application needs a server`() = runTest {
        val subject = getTestSubject {
            AttemptedServerConnection.Success(
                TestServerConnection()
            )
        }
        subject.loadSettings()
        subject.server.value shouldBe PairingState.NotPaired
    }

    @Test
    fun `when settings are available the client connects to server`() = runTest {
        val user = userArb.next()
        val serializedUser = Json.encodeToString(user)
        val serverContacted = MutableStateFlow(false)
        val connection = TestServerConnection {
            serverContacted.value = true
        }
        val subject =
            getTestSubject(
                mapOf(
                    "serverAddress" to "localhost", "serverPort" to "8080",
                    UserKey to serializedUser
                )
            ) {
                AttemptedServerConnection.Success(connection)
            }
        subject.loadSettings()
        // this will fail due to time out, or succeed if the connection is made
        serverContacted.filter { !it }.first()
    }

}
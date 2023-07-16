import com.xingpeds.alldone.entities.*
import com.xingpeds.alldone.entities.test.*
import com.xingpeds.alldone.shared.logic.AttemptedServerConnection
import com.xingpeds.alldone.shared.logic.ClientApplication
import com.xingpeds.alldone.shared.logic.ClientState
import com.xingpeds.alldone.shared.logic.ConnectionToServerFun
import com.xingpeds.alldone.shared.logic.GetUserDetailsAndServerUrlFun
import com.xingpeds.alldone.shared.logic.NewUserAndServer
import com.xingpeds.alldone.shared.logic.PersistedSettings
import com.xingpeds.alldone.shared.logic.ServerConnection
import com.xingpeds.alldone.shared.logic.Url
import com.xingpeds.alldone.shared.logic.UserInputManager
import com.xingpeds.alldone.shared.logic.stateKey
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test


class TestSettings(settings: Map<String, String> = mapOf()) : PersistedSettings {
    private val map = settings.toMutableMap()
    override suspend fun get(key: String): String? = map[key].also {
        println("loaded $key = $it")
    }

    override suspend fun set(key: String, value: String) = map.set(key, value).also {
        println("saved $key to $value")
    }
}

class TestUserInputManager(val userAndUrlFromUser: NewUserAndServer? = null) : UserInputManager {
    override val getUserDetailsAndServerUrl: GetUserDetailsAndServerUrlFun
        get() = object : GetUserDetailsAndServerUrlFun {
            override suspend fun invoke(): NewUserAndServer {
                if (userAndUrlFromUser == null) {
                    // wait forever
                    while (true) {
                        delay(1000)
                    }
                } else {
                    return userAndUrlFromUser
                }
            }
        }
}

val testCoroutineExceptionHandler = CoroutineExceptionHandler { context, exception ->
    val message = """
        context: $context
        exception: $exception
    """.trimIndent()
    println(message)
}

class ClientApplicationTestTDD {
    fun TestScope.getTestSubject(
        settings: Map<String, String> = mapOf(),
        userInputManager: TestUserInputManager = TestUserInputManager(),
        connectToServer: ConnectionToServerFun = object : ConnectionToServerFun {
            override suspend fun invoke(
                p1: Url,
                coroutineScope: CoroutineScope,
            ): AttemptedServerConnection {
                return AttemptedServerConnection.Failure.ConnectionRefused
            }
        },
    ) = ClientApplication(
        settings = TestSettings(settings),
        connectionToServer = connectToServer,
        appScope = this.backgroundScope.plus(testCoroutineExceptionHandler),
        userInput = userInputManager,
        autoStart = false
    )

    @Test
    fun construct() = runTest {
        getTestSubject()
        // if this test completes it is successful
    }

    @Test
    fun `device key is available after start`() = runTest {

        val subject = getTestSubject()
        subject.start().join()
        subject.getDevice()
        //if this test completes it is successful
    }

    @Test
    fun `app with no saved configuration starts in tabularasa state`() = runTest {
        val subject = getTestSubject()
        subject.start().join()
        subject.stateFlow.filterNotNull().first() shouldBe ClientState.TabulaRasa
    }

    @Test
    fun `after user has supplied app with data it tries to register`() = runTest {
        val url = Url("http://localhost:8080")
        val input = TestUserInputManager(
            userAndUrlFromUser = NewUserAndServer(
                UserData("user"),
                url,
            )
        )
        val connectFun = object : ConnectionToServerFun {
            override suspend fun invoke(p1: Url, p2: CoroutineScope): AttemptedServerConnection {
                p1 shouldBe url
                return AttemptedServerConnection.Failure.ConnectionRefused
            }
        }
        val subject = getTestSubject(connectToServer = connectFun, userInputManager = input)
        subject.start().join()
        subject.stateFlow.filterNotNull().first().shouldBeTypeOf<ClientState.AttemptingToRegister>()
        val arr = Array<Int>(10000) { it }
        val sum = arr.fold(0) { acc, i -> acc + i }
        println(sum)
    }

    @Test
    fun `when the server responds with success the app moves into paired state`() = runTest {
        val url = Url("http://localhost:8080")
        val userData = UserData("user")
        val input = TestUserInputManager(
            userAndUrlFromUser = NewUserAndServer(
                userData,
                url,
            )
        )
        val connectFun = object : ConnectionToServerFun {
            override suspend fun invoke(p1: Url, p2: CoroutineScope): AttemptedServerConnection {
                p1 shouldBe url
                return AttemptedServerConnection.Success(
                    object : ServerConnection {
                        override val serverAddress: Url
                            get() = p1

                        override suspend fun send(message: ClientMessage) {
                            require(message is NewUserRequest)
                            message.userData shouldBe userData
                        }

                        override val incoming: Flow<ServerMessage>
                            get() = flowOf(NewUserResponse(userArb.next()))
                    }
                )
            }
        }
        val subject = getTestSubject(connectToServer = connectFun, userInputManager = input)
        subject.start().join()
        subject.stateFlow.filterNotNull().filterIsInstance<ClientState.Paired>().first()
        // if this test completes it is successful
    }

    @Test
    fun `when starting in paired state app identifies`() = runTest {
        val url = Url("http://localhost:8080")
        val user = userArb.next()
        val testState = MutableStateFlow(false)
        val settingsMap = mapOf(
            stateKey to Json.encodeToString(ClientState.Paired(user, url) as ClientState)
        )
        val connectFun = object : ConnectionToServerFun {
            override suspend fun invoke(p1: Url, p2: CoroutineScope): AttemptedServerConnection {
                p1 shouldBe url
                return AttemptedServerConnection.Success(
                    object : ServerConnection {
                        override val serverAddress: Url
                            get() = p1

                        override suspend fun send(message: ClientMessage) {
                            println("send called with: $message")
                            require(message is IdentifyUser)
                            message.user shouldBe user
                            testState.emit(true)
                        }

                        override val incoming: Flow<ServerMessage>
                            get() = flowOf(IdentifySuccess)
                    }
                )
            }
        }
        val subject = getTestSubject(connectToServer = connectFun, settings = settingsMap)
        subject.start().join()
        subject.stateFlow.filterNotNull().filterIsInstance<ClientState.Paired>().first()
        testState.filter { it }.first()
        // if this test completes it is successful
    }

}
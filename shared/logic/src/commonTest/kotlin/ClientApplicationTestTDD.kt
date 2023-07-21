import com.xingpeds.alldone.entities.*
import com.xingpeds.alldone.entities.test.*
import com.xingpeds.alldone.shared.logic.AttemptedServerConnection
import com.xingpeds.alldone.shared.logic.ClientApplication
import com.xingpeds.alldone.shared.logic.ClientData
import com.xingpeds.alldone.shared.logic.ClientState
import com.xingpeds.alldone.shared.logic.ConnectionToServerFun
import com.xingpeds.alldone.shared.logic.MemoryNonPersistentSettings
import com.xingpeds.alldone.shared.logic.NewUserAndServer
import com.xingpeds.alldone.shared.logic.ServerConnection
import com.xingpeds.alldone.shared.logic.Url
import com.xingpeds.alldone.shared.logic.UserInputManager
import com.xingpeds.alldone.shared.logic.stateKey
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test


class TestUserInputManager(val userAndUrlFromUser: NewUserAndServer? = null) : UserInputManager {

    override suspend fun getUserDetailsAndServerUrl(): NewUserAndServer {
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


class TestData(
    val onAddTask: (Task) -> Unit = {},
    val onResetCompletionsForTask: (UUID, List<Completion>) -> Unit = { _, _ -> },
) : ClientData {
    override suspend fun addTask(task: Task) {
        println("task saved: $task")
        onAddTask(task)
    }

    override suspend fun resetCompletionsForTask(taskId: UUID, completions: List<Completion>) {
        TODO("Not yet implemented")
    }

    override suspend fun resetTasks(tasks: List<Task>) {
        TODO("Not yet implemented")
    }

    override suspend fun addCompletion(completion: Completion) {
        TODO("Not yet implemented")
    }
}

class ClientApplicationTestTDD {
    fun TestEnvironment.getTestSubject(
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
        clientData: ClientData = TestData(),
    ) = ClientApplication(
        settings = MemoryNonPersistentSettings(settings),
        connectionToServer = connectToServer,
        appScope = testScope,
        userInput = userInputManager,
        autoStart = false,
        exceptionHandler = this.handler,
        clientData = clientData,
    )

    @Test
    fun construct() = runTestMultiThread {
        getTestSubject()
        // if this test completes it is successful
    }

    @Test
    fun `device key is available after start`() = runTestMultiThread {

        val subject = getTestSubject()
        subject.start().join()
        subject.getDevice()
        //if this test completes it is successful
    }

    @Test
    fun `app with no saved configuration starts in tabularasa state`() = runTestMultiThread {
        val subject = getTestSubject()
        subject.start().join()
        subject.stateFlow.filterNotNull().first() shouldBe ClientState.TabulaRasa
    }

    @Test
    fun `after user has supplied app with data it tries to register`() = runTestMultiThread {
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
        subject.stateFlow.filterIsInstance<ClientState.AttemptingToRegister>().first()
    }

    @Test
    fun `when the server responds with success the app moves into paired state`() =
        runTestMultiThread {
            val url = Url("http://localhost:8080")
            val userData = UserData("user")
            val input = TestUserInputManager(
                userAndUrlFromUser = NewUserAndServer(
                    userData,
                    url,
                )
            )
            val connectFun = object : ConnectionToServerFun {
                override suspend fun invoke(
                    p1: Url,
                    p2: CoroutineScope,
                ): AttemptedServerConnection {
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
                            override val connectionScope: CoroutineScope
                                get() = p2
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
    fun `when starting in paired state app identifies`() = runTestMultiThread {
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
                        override val connectionScope: CoroutineScope
                            get() = p2
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

    @Test
    fun `app saves all incoming tasks`() = runTestMultiThread {
        val task = taskArb.next()
        val testFinished = MutableStateFlow(false)
        val clientData = TestData(onAddTask = {
            it shouldBe task
            testFinished.value = true
        })
        val subject = getTestSubject(clientData = clientData)
        val serverMessage = AddTaskResponse(task)
        subject.handleServerMessage(serverMessage)
        testFinished.filter { it }.first()
    }

}
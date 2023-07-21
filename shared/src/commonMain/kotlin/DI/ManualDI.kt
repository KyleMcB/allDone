package DI

import com.xingpeds.alldone.entities.*
import com.xingpeds.alldone.shared.logic.AttemptedServerConnection
import com.xingpeds.alldone.shared.logic.ClientApplication
import com.xingpeds.alldone.shared.logic.ClientData
import com.xingpeds.alldone.shared.logic.ConnectionToServerFun
import com.xingpeds.alldone.shared.logic.NewUserAndServer
import com.xingpeds.alldone.shared.logic.PersistedSettings
import com.xingpeds.alldone.shared.logic.Url
import com.xingpeds.alldone.shared.logic.UserInputManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

val fakeSettings: PersistedSettings = object : PersistedSettings {
    val map = mutableMapOf<String, String>()
    override suspend fun get(key: String): String? = map[key]

    override suspend fun set(key: String, value: String) {
        map.put(key, value)
    }
}

val connectToServerKtorClient: ConnectionToServerFun = object : ConnectionToServerFun {
    override suspend fun invoke(p1: Url, p2: CoroutineScope): AttemptedServerConnection {
        TODO("Not yet implemented")
    }
}

enum class UserInputRequests {
    GetUserDetailsAndServerUrl, None
}

interface InputUI {
    val inputState: StateFlow<UserInputRequests?>
    fun submitUserDetailsAndServerUrl(userDetails: UserData, serverUrl: Url)
}

// todo zomg I need to navigate to a user input screen instead of a stateflow
class ComposeUserInputManager : UserInputManager, InputUI {
    private val state = MutableStateFlow<UserInputRequests>(UserInputRequests.None)
    override val inputState: StateFlow<UserInputRequests?>
        get() = state
    private var userAndUrlFromUserFinished: ((NewUserAndServer) -> Unit)? = null

    override fun submitUserDetailsAndServerUrl(userDetails: UserData, serverUrl: Url) {
        val finish = userAndUrlFromUserFinished
        require(finish != null)
        finish(NewUserAndServer(userDetails, serverUrl))
        userAndUrlFromUserFinished = null
        state.value = UserInputRequests.None
    }

    override suspend fun getUserDetailsAndServerUrl(): NewUserAndServer {
        state.value = UserInputRequests.GetUserDetailsAndServerUrl
        return suspendCoroutine { continuation ->
            userAndUrlFromUserFinished = { newuserAndServer ->
                continuation.resume(newuserAndServer)
            }
        }
    }

}

val clientData = object : ClientData {
    override suspend fun addTask(task: Task) {
        TODO("Not yet implemented")
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

val app = ClientApplication(
    fakeSettings,
    connectToServerKtorClient,
    ComposeUserInputManager(),
    clientData
)

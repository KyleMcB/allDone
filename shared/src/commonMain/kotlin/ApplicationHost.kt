import com.xingpeds.alldone.entities.*
import com.xingpeds.alldone.shared.logic.AttemptedServerConnection
import com.xingpeds.alldone.shared.logic.ClientApplication
import com.xingpeds.alldone.shared.logic.ClientData
import com.xingpeds.alldone.shared.logic.ConnectionToServerFun
import com.xingpeds.alldone.shared.logic.GetUserDetailsAndServerUrlFun
import com.xingpeds.alldone.shared.logic.PersistedSettings
import com.xingpeds.alldone.shared.logic.Url
import com.xingpeds.alldone.shared.logic.UserInputManager
import kotlinx.coroutines.CoroutineScope

val fakeSettings: PersistedSettings = object : PersistedSettings {
    val map = mutableMapOf<String, String>()
    override suspend fun get(key: String): String? = map[key]

    override suspend fun set(key: String, value: String): Unit {
        map.put(key, value)
    }
}

val connectToServerKtorClient: ConnectionToServerFun = object : ConnectionToServerFun {
    override suspend fun invoke(p1: Url, p2: CoroutineScope): AttemptedServerConnection {
        TODO("Not yet implemented")
    }
}
val inputManager = object : UserInputManager {
    override val getUserDetailsAndServerUrl: GetUserDetailsAndServerUrlFun
        get() = TODO("Not yet implemented")
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

val app = ClientApplication(fakeSettings, connectToServerKtorClient, inputManager, clientData)

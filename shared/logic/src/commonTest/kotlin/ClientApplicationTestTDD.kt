import com.xingpeds.alldone.shared.logic.AttemptedServerConnection
import com.xingpeds.alldone.shared.logic.ClientApplication
import com.xingpeds.alldone.shared.logic.ConnectionToServerFun
import com.xingpeds.alldone.shared.logic.Navigator
import com.xingpeds.alldone.shared.logic.PersistedSettings
import com.xingpeds.alldone.shared.logic.Screen
import com.xingpeds.alldone.shared.logic.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TestNavigator : Navigator {
    val events = MutableSharedFlow<Screen>(replay = 1)
    override suspend fun navigateTo(screen: Screen) {
        events.emit(screen)
    }

}

class ClientApplicationTestTDD {
    fun TestScope.getTestSubject(
        settings: Map<String, String> = mapOf(),
        connectToServer: ConnectionToServerFun = object : ConnectionToServerFun {
            override suspend fun invoke(
                p1: Url,
                coroutineScope: CoroutineScope,
            ): AttemptedServerConnection {
                return AttemptedServerConnection.Failure.ConnectionRefused
            }
        },
        navigator: Navigator = TestNavigator(),
    ) = ClientApplication(
        settings = object : PersistedSettings {
            val map = settings.toMutableMap()
            override suspend fun get(key: String): String? = map[key]

            override suspend fun set(key: String, value: String) = map.set(key, value)
        },
        connectionToServer = connectToServer,
        navigator = navigator,
    )

    @Test
    fun construct() = runTest {
        getTestSubject()
    }


}
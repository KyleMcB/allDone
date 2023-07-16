import com.xingpeds.alldone.shared.logic.AttemptedServerConnection
import com.xingpeds.alldone.shared.logic.ClientApplication
import com.xingpeds.alldone.shared.logic.ClientState
import com.xingpeds.alldone.shared.logic.ConnectionToServerFun
import com.xingpeds.alldone.shared.logic.Navigator
import com.xingpeds.alldone.shared.logic.PersistedSettings
import com.xingpeds.alldone.shared.logic.Screen
import com.xingpeds.alldone.shared.logic.Url
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class TestNavigator : Navigator {
    val events = MutableSharedFlow<Screen>(replay = 1)
    override suspend fun navigateTo(screen: Screen) {
        events.emit(screen)
    }

}

class TestSettings(settings: Map<String, String> = mapOf()) : PersistedSettings {
    private val map = settings.toMutableMap()
    override suspend fun get(key: String): String? = map[key].also {
        println("loaded $key = $it")
    }

    override suspend fun set(key: String, value: String) = map.set(key, value).also {
        println("saved $key to $value")
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
        settings = TestSettings(settings),
        connectionToServer = connectToServer,
        navigator = navigator,
        appScope = this.backgroundScope,
    )

    @Test
    fun construct() = runTest {
        getTestSubject()
        // if this test completes it is successful
    }

    @Test
    fun `device key is available after start`() = runTest {
        val navigator = TestNavigator()
        val subject = getTestSubject(navigator = navigator)
        subject.start().join()
        subject.getDevice()
        //if this test completes it is successful
    }

    @Test
    fun `app with no saved configuration starts in tabularasa state`() = runTest {
        val navigator = TestNavigator()
        val subject = getTestSubject(navigator = navigator)
        subject.start().join()
        subject.stateFlow.filterNotNull().first() shouldBe ClientState.TabulaRasa
    }

}
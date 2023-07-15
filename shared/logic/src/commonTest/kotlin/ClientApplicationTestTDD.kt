import com.xingpeds.alldone.shared.logic.ClientApplication
import com.xingpeds.alldone.shared.logic.PairingState
import com.xingpeds.alldone.shared.logic.PersistedSettings
import com.xingpeds.alldone.shared.logic.Url
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ClientApplicationTestTDD {
    fun TestScope.getTestSubject(map: Map<String, String> = mapOf()): ClientApplication {
        val clientApplication = ClientApplication(
            object : PersistedSettings {
                override suspend fun get(key: String): String? {
                    return map[key]
                }
            },
            dispatcher = UnconfinedTestDispatcher(scheduler = this.testScheduler)
        )
        backgroundScope.launch {
            clientApplication.server.collect {
                println(it)
            }
        }
        return clientApplication
    }

    // can not test the loading state because it loads too quickly and the stateflow skips it initial state
//    @Test
    fun `application starts in loading state`() = runTest {
        val subject = getTestSubject()
        subject.server.value shouldBe PairingState.Loading
    }

    @Test
    fun `application loads settings`() = runTest {
        val subject = getTestSubject(mapOf("serverAddress" to "localhost", "serverPort" to "8080"))
        subject.loadSettings()
        subject.server.value shouldBe PairingState.Paired(Url("ws://localhost:8080/ws"))
    }

    @Test
    fun `application needs a server`() = runTest {
        val subject = getTestSubject()
        subject.loadSettings()
        subject.server.value shouldBe PairingState.NotPaired
    }

}
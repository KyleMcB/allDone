import TestClientConnection.Companion.getTestClient
import com.xingpeds.alldone.entities.*
import com.xingpeds.alldone.entities.test.*
import com.xingpeds.taskserver.logic.Authenticator
import com.xingpeds.taskserver.logic.MemoryNonPersistence
import com.xingpeds.taskserver.logic.login
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LoginTest {
    @Test
    fun `login asks to identify before other messages`() = runTest {
        val connection = getTestClient(
            flowOf(
                AddTask(taskArb.next().asData()),
            )
        )
        val data = MemoryNonPersistence()
        val authenticator = Authenticator(data)

        authenticator.login(connection)

        connection.outbound.value.first() shouldBe IdentifyFirst
    }

    @Test
    fun `login stops listening after 3 attempts`() = runTest {
        val connection = getTestClient(
            flowOf(
                AddTask(taskArb.next().asData()),
                AddTask(taskArb.next().asData()),
                AddTask(taskArb.next().asData()),
                AddTask(taskArb.next().asData()),
            )
        )
        val data = MemoryNonPersistence()
        val authenticator = Authenticator(data)

        authenticator.login(connection)

        connection.outbound.value.size shouldBe 3
    }

    @Test
    fun `connection requests new user`() = runTest {
        val connection = getTestClient(
            flowOf(
                NewUserRequest(UserData("test"), deviceArb.next()),
            )
        )
        val data = MemoryNonPersistence()
        val authenticator = Authenticator(data)
        authenticator.login(connection).shouldNotBeNull()
    }

    @Test
    fun `connection identifies as a user`() = runTest {
        val user = userArb.next()
        val device = deviceArb.next()
        val connection = getTestClient(
            flowOf(
                IdentifyUser(user, device)
            )
        )
        val data = MemoryNonPersistence()
        val authenticator = Authenticator(data)
        authenticator.login(connection).shouldNotBeNull()
    }
}
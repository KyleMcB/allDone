import TestClientConnection.Companion.getTestClient
import com.example.logic.Persistence
import com.example.logic.SingleUserServer
import com.xingpeds.alldone.entities.test.*
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class SingleUserServerTest {
    val subjectUser = userArb.next()
    val data = object : Persistence {

    }

    fun getTestSubject(): SingleUserServer = SingleUserServer(
        user = subjectUser,
        data = data
    )

    @Test
    fun `addConnection should add a connection to the connections`() = runTest {
        val subject = getTestSubject()
        val connection = clientConnectionArb.next()

        subject.addConnection(connection)

        subject.connections.value.contains(connection)
    }

    @Test
    fun `adding a duplicate connection does nothing`() = runTest {
        val subject = getTestSubject()
        val connection = clientConnectionArb.next()
        repeat(2) { subject.addConnection(connection) }
        assertEquals(1, subject.connections.value.size)
    }

    @Test
    fun `adding two different connections adds both`() = runTest {
        val subject = getTestSubject()
        val connection1 = clientConnectionArb.next()
        val connection2 = clientConnectionArb.next()
        subject.addConnection(connection1)
        subject.addConnection(connection2)
        assertEquals(2, subject.connections.value.size)
    }

    @Test
    fun `broadcast should send a message to all connections`() = runTest {
        val subject = getTestSubject()
        val connection1 = getTestClient()
        val connection2 = getTestClient()
        subject.addConnection(connection1)
        subject.addConnection(connection2)
        assertEquals(2, subject.connections.value.size)
        val message = serverMessageArb.next()

        subject.broadcast(message).join()

        assertEquals(message, connection1.outbound.value.single())
        assertEquals(message, connection2.outbound.value.single())
        assertEquals(1, connection1.outbound.value.size)
        assertEquals(1, connection2.outbound.value.size)
    }

}
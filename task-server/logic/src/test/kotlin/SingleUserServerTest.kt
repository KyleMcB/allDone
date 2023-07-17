import TestClientConnection.Companion.getTestClient
import com.xingpeds.alldone.entities.*
import com.xingpeds.alldone.entities.test.*
import com.xingpeds.taskserver.logic.MemoryNonPersistence
import com.xingpeds.taskserver.logic.Persistence
import com.xingpeds.taskserver.logic.SingleUserServer
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.next
import io.kotest.property.forAll
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class SingleUserServerTest {
    val subjectUser = userArb.next()


    fun getTestSubject(data: Persistence = MemoryNonPersistence()): SingleUserServer =
        SingleUserServer(
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

    @Test
    fun `user can get tasks (empty)`() = runTest {
        val subject = getTestSubject()
        val connection = getTestClient(flowOf(AllTasks))
        subject.addConnection(connection)
        val response =
            connection.outbound.filterNot { it.isEmpty() }.first().first() as AllTasksResponse
        assertEquals(emptyList(), response.tasks)
    }

    @Test
    fun `user can get tasks (populated)`() = runTest {
        val task = taskArb.next()
        val data = MemoryNonPersistence(mutableMapOf(subjectUser to listOf(task)))
        val subject = getTestSubject(data)
        val connection = getTestClient(flowOf(AllTasks))
        subject.addConnection(connection)
        val response =
            connection.outbound.filterNot { it.isEmpty() }.first().first() as AllTasksResponse
        assertEquals(listOf(task), response.tasks)
    }

    @Test
    fun `property test, all saved tasks will be sent with AllTasks`() = runTest {
        forAll(Arb.list(taskArb)) { taskList ->
            val data = MemoryNonPersistence(mutableMapOf(subjectUser to taskList))
            val subject = getTestSubject(data)
            val connection = getTestClient(flowOf(AllTasks))
            subject.addConnection(connection)
            val response = connection.outbound.filterNot { it.isEmpty() }
                .first().first() as AllTasksResponse
            response.tasks == taskList
        }
    }

    @Test
    fun `user can add a task`() = runTest {
        val task = taskArb.next()
        val subject = getTestSubject()
        val connection = getTestClient(flowOf(AddTask(task.asData())))
        subject.addConnection(connection)
        val response =
            connection.outbound.filterNot { it.isEmpty() }.first().first() as AddTaskResponse
    }

    @Test
    fun `user can create a completion`() = runTest {
        val task = taskArb.next()
        val subject = getTestSubject()
        val connection =
            getTestClient(
                flowOf(
                    CreateCompletion(
                        task,
                        CompletionData(task.id, instantArb.next())
                    )
                )
            )
        subject.addConnection(connection)
        val response =
            connection.outbound.filterNot { it.isEmpty() }.first()
                .first() as CreateCompletionResponse
    }

    @Test
    fun `user can get a completion dump for a task`() = runTest {
        val task = taskArb.next()
        val completion = completionArbFactory(task.id).next()
        val data = MemoryNonPersistence(
            taskMap =
            mutableMapOf(
                subjectUser to listOf(task),
            ),
            completionMap = mutableMapOf(
                task.id to listOf(completion)
            )
        )
        val subject = getTestSubject(data)
        val connection = getTestClient(flowOf(AllCompletionsForTask(task.id)))
        subject.addConnection(connection)
        connection.outbound.filterNot { it.isEmpty() }.first()
            .first() as AllCompletionsForTaskResponse
    }
}

fun Task.asData() = TaskData(
    name = name,
    type = type,
    notificationType = notificationType,
    due = due
)
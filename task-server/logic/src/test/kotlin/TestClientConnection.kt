import com.xingpeds.alldone.entities.*
import com.xingpeds.taskserver.logic.ClientConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope

class TestClientConnection private constructor(
    scope: CoroutineScope, messages: Flow<ClientMessage>,
) : ClientConnection {
    private val _outbound = MutableStateFlow<List<ServerMessage>>(emptyList())
    val outbound = _outbound.asStateFlow()
    override val inbound: Flow<ClientMessage> = messages

    override suspend fun send(message: ServerMessage) {
        println("$this get $message")
        _outbound.update { it + message }
    }

    init {
        scope.launch {
            //This is make individual value checks up-to-date
            outbound.collect()
        }
    }

    companion object {

        fun TestScope.getTestClient(messages: Flow<ClientMessage> = flowOf()) =
            TestClientConnection(this.backgroundScope, messages)
    }
}
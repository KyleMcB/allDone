import com.example.logic.ClientConnection
import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope

class TestClientConnection private constructor(scope: CoroutineScope) : ClientConnection {
    private val _outbound = MutableStateFlow<List<ServerMessage>>(emptyList())
    val outbound = _outbound.asStateFlow()
    override suspend fun send(message: ServerMessage) {
        _outbound.update { it + message }
        println("$this get $message")
    }

    init {
        scope.launch {
            //This is make individual value checks up-to-date
            outbound.collect()
        }
    }

    companion object {

        fun TestScope.getTestClient() = TestClientConnection(this.backgroundScope)
    }
}
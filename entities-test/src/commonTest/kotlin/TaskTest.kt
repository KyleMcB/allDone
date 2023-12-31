import com.xingpeds.alldone.entities.*
import com.xingpeds.alldone.entities.test.*
import io.kotest.property.forAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class TaskTest {
    @Test
    fun serializableProperty() = runTest {
        forAll(taskArb) { task ->
            val string: String = Json.encodeToString(task)
            task == Json.decodeFromString<Task>(string)
        }
    }

}
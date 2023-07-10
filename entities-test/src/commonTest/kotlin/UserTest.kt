import com.xingpeds.alldone.entities.*
import com.xingpeds.alldone.entities.test.*
import io.kotest.property.forAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class UserTest {
    @Test
    fun userIsSerializable() = runTest {
        forAll(userArb) { user ->
            val string: String = Json.encodeToString(user)
            user == Json.decodeFromString<User>(string)
        }
    }
}
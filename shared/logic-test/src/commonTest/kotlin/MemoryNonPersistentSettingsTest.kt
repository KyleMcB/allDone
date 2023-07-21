import com.xingpeds.alldone.shared.logic.MemoryNonPersistentSettings
import com.xingpeds.alldone.shared.logic.PersistedSettings
import com.xingpeds.alldone.shared.logic.PersistentSettingsTestTemplate
import kotlin.test.Test

class MemoryNonPersistentSettingsTest : PersistentSettingsTestTemplate {
    override fun getTestSubject(): PersistedSettings = MemoryNonPersistentSettings()

    @Test
    override fun `save and retrieve settings`() {
        super.`save and retrieve settings`()
    }
}
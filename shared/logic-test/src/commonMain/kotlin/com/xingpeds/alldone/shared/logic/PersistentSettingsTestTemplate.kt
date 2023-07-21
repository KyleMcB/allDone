package com.xingpeds.alldone.shared.logic

import com.xingpeds.alldone.entities.test.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.forAll
import kotlin.test.Test

interface PersistentSettingsTestTemplate {
    fun getTestSubject(): PersistedSettings

    @Test
    fun `save and retrieve settings`() = runTestMultiThread {
        forAll(Arb.string(), Arb.string()) { key, value ->
            val settings = getTestSubject()
            settings.set(key, value)
            settings.get(key) == value
        }
    }
}


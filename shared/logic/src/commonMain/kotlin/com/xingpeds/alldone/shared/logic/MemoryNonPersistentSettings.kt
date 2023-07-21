package com.xingpeds.alldone.shared.logic

class MemoryNonPersistentSettings(settings: Map<String, String> = mapOf()) : PersistedSettings {
    private val map = settings.toMutableMap()
    override suspend fun get(key: String): String? = map[key].also {
        println("loaded $key = $it")
    }

    override suspend fun set(key: String, value: String) = map.set(key, value).also {
        println("saved $key to $value")
    }
}
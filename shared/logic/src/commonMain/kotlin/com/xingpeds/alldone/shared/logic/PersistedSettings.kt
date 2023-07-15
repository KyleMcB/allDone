package com.xingpeds.alldone.shared.logic

interface PersistedSettings {
    suspend fun get(key: String): String?
    suspend fun set(key: String, value: String)

}

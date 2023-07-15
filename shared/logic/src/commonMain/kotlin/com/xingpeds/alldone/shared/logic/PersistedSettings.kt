package com.xingpeds.alldone.shared.logic

interface PersistedSettings {
    suspend fun get(key: String): String?

}

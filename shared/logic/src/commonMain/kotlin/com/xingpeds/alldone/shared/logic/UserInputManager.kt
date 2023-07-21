package com.xingpeds.alldone.shared.logic

import com.xingpeds.alldone.entities.*

interface UserInputManager {
    suspend fun getUserDetailsAndServerUrl(): NewUserAndServer

}

typealias NewUserAndServer = Pair<UserData, Url>

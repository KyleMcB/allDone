package com.xingpeds.alldone.shared.logic

import com.xingpeds.alldone.entities.*

interface UserInputManager {
    val getUserDetailsAndServerUrl: GetUserDetailsAndServerUrlFun

}

typealias NewUserAndServer = Pair<UserData, Url>

interface GetUserDetailsAndServerUrlFun : suspend () -> NewUserAndServer
package com.xingpeds.alldone.entities

import kotlinx.serialization.Serializable

@Serializable
data class User(val name: String, val id: UUID) //: UserData
interface UserData {
    val name: String
}

@Serializable
data class NewUserRequest(override val name: String) : UserData, ClientMessage

@Serializable
data class NewUserResponse(val user: User) : ServerMessage
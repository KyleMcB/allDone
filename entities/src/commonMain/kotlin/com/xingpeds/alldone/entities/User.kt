package com.xingpeds.alldone.entities

import kotlinx.serialization.Serializable

@Serializable
data class User(override val name: String, val id: UUID) : IUser

@Serializable
sealed interface IUser {
    val name: String
}

@Serializable
data class UserData(override val name: String) : IUser

@Serializable
data class Device(val id: UUID)

@Serializable
data class NewUserRequest(val userData: UserData, val device: Device) : PreAuthClientMessage

@Serializable
data class NewUserResponse(val user: User) : ServerMessage

@Serializable
data class IdentifyUser(val user: User, val device: Device) : PreAuthClientMessage
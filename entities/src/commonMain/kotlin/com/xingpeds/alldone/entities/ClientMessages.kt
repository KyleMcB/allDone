package com.xingpeds.alldone.entities

import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthenticatedClientMessage : ClientMessage

@Serializable
object AllTasks : AuthenticatedClientMessage
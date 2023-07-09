package com.xingpeds.alldone.entities

import kotlinx.serialization.Serializable

@Serializable
sealed interface ClientMessage

@Serializable
sealed interface IdentifiedRequest : ClientMessage

@Serializable
sealed interface ServerMessage

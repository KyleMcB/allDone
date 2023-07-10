package com.xingpeds.alldone.entities

import kotlinx.serialization.Serializable

@Serializable
data class AllTasksResponse(val tasks: List<Task>) : ServerMessage

@Serializable
object InvalidRequest : ServerMessage
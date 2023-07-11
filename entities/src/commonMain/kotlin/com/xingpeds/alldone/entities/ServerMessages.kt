package com.xingpeds.alldone.entities

import kotlinx.serialization.Serializable

@Serializable
data class AllTasksResponse(val tasks: List<Task>) : ServerMessage

@Serializable
object InvalidRequest : ServerMessage

@Serializable
data class AddTaskResponse(val task: Task) : ServerMessage

@Serializable
data class CreateCompletionResponse(val completion: Completion) : ServerMessage

@Serializable
data class AllCompletionsForTaskResponse(val taskId: UUID, val completions: List<Completion>) :
    ServerMessage
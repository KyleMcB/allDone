package com.xingpeds.alldone.entities

import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthenticatedClientMessage : ClientMessage

@Serializable
object AllTasks : AuthenticatedClientMessage

@Serializable
data class AddTask(val taskData: TaskData) : AuthenticatedClientMessage

@Serializable
data class CreateCompletion(val task: Task, val completionData: CompletionData) :
    AuthenticatedClientMessage

@Serializable
data class AllCompletionsForTask(val taskId: UUID) : AuthenticatedClientMessage
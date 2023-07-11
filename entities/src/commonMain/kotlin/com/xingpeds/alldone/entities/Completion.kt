package com.xingpeds.alldone.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Completion(override val taskId: UUID, val id: UUID, override val timeStamp: Instant) :
    ICompletion

interface ICompletion {
    val taskId: UUID
    val timeStamp: Instant
}

@Serializable
data class CompletionData(override val taskId: UUID, override val timeStamp: Instant) : ICompletion

package com.xingpeds.alldone.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    override val name: String,
    val id: UUID,
    override val type: RepeatType,
    override val notificationType: NotificationType? = null,
    override val due: Instant? = null,
) : ITask

interface ITask {
    val name: String
    val type: RepeatType
    val notificationType: NotificationType?
    val due: Instant?
}

@Serializable
data class TaskData(
    override val name: String,
    override val type: RepeatType,
    override val notificationType: NotificationType?,
    override val due: Instant?
) : ITask

@Serializable
enum class RepeatType(val interval: Int = 1) {
    Single,
    Daily,
    Weekly,
    Monthly
}

@Serializable
enum class NotificationType {
    None,
    Warning,
    Alarm,
    WarningAndAlarm
}

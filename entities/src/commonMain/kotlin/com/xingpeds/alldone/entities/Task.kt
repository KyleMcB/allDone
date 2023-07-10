package com.xingpeds.alldone.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val name: String,
    val id: UUID,
    val type: RepeatType,
    val notificationType: NotificationType? = null,
    val due: Instant? = null,
)

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

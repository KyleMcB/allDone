package com.xingpeds.alldone.entities

import kotlinx.serialization.Serializable

@Serializable
data class Task(
    val name: String,
    val id: UUID,
    val type: RepeatType,
    val repeatInterval: Int,
    val notificationType: NotificationType
)

@Serializable
enum class RepeatType {
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

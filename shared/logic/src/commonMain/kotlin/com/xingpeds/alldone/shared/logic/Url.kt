package com.xingpeds.alldone.shared.logic

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class Url(val value: String) : CharSequence by value {
//todo validate url
}

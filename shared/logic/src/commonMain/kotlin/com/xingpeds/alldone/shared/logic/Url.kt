package com.xingpeds.alldone.shared.logic

import kotlin.jvm.JvmInline

@JvmInline
value class Url(val value: String) : CharSequence by value {
//todo validate url
}

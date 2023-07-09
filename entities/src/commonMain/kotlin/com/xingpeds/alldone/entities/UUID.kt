/*
 * Copyright (c) Kyle McBurnett 2023.
 */

package com.xingpeds.alldone.entities

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
import com.benasher44.uuid.uuidOf
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias UUID = @Serializable(with = UUIDSerializer::class) Uuid

fun uuidFrom(string: String): UUID = uuidFrom(string)
fun randUuid(): UUID = uuid4()
fun uuidFrom(bytes: ByteArray): UUID = uuidOf(bytes)

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("uuid", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID =
        uuidFrom(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: UUID) =
        encoder.encodeString(value.toString())

}
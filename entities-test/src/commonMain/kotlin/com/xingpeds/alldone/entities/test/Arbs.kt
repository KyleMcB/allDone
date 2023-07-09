package com.xingpeds.alldone.entities.test

import com.xingpeds.alldone.entities.*
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string

val uuidArb = arbitrary { rs ->
    uuidFrom(Arb.byteArray(Arb.int(min = 16, max = 16), Arb.byte()).bind())
}

val taskArb = arbitrary {
    Task(
        name = Arb.string().bind(),
        id = uuidArb.bind(),
        type = Arb.enum<RepeatType>().bind(),
        repeatInterval = Arb.int().bind(),
        notificationType = Arb.enum<NotificationType>().bind()
    )
}

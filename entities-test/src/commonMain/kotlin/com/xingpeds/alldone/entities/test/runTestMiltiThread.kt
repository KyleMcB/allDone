package com.xingpeds.alldone.entities.test

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class TestInvironmentData(
    override val testScope: CoroutineScope,
    override val execScope: CoroutineScope,
    override val handler: CoroutineExceptionHandler,
) : TestEnvironment

interface TestEnvironment {
    val testScope: CoroutineScope
    val execScope: CoroutineScope
    val handler: CoroutineExceptionHandler

}

fun runTestMultiThread(timeOut: Duration = 10.seconds, block: suspend TestEnvironment.() -> Unit) =
    runBlocking {

        val execScope = this
        val execeptionHandler = CoroutineExceptionHandler { context, exception ->
            println(
                """
                context: $context
                exception: $exception
            """.trimIndent()
            )
            execScope.cancel("test failed", exception)
        }

        val testJob = Job()
        val context = Dispatchers.Default + testJob + execeptionHandler
        val env = TestInvironmentData(CoroutineScope(context), execScope, execeptionHandler)

        withTimeout(timeOut) {
            env.block()
        }
        testJob.cancelAndJoin()
    }
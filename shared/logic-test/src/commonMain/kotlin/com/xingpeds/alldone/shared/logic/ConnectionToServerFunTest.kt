package com.xingpeds.alldone.shared.logic

import com.xingpeds.alldone.entities.test.*
import kotlin.test.Test

//not sure this is the right direction
interface ConnectionToServerFunTest {
    fun getTestSubject(): Pair<ConnectionToServerFun, Url>

    @Test
    fun explore() = runTestMultiThread {
        val (testSubject, testUrl) = getTestSubject()
        testSubject(testUrl, this.testScope)

    }
}
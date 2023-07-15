package com.xingpeds.alldone.shared.logic

interface Navigator {

    suspend fun navigateTo(screen: Screen)
}

sealed class Screen {
    object TabulaRasa : Screen()
}
package com.xingpeds.alldone.shared.logic

import com.xingpeds.alldone.entities.*

interface ClientData {
    suspend fun addTask(task: Task)
    suspend fun resetCompletionsForTask(taskId: UUID, completions: List<Completion>)
    suspend fun resetTasks(tasks: List<Task>)

    suspend
    fun addCompletion(completion: Completion)

}

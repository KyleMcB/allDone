package com.example.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.flow.Flow

interface Persistence {
    suspend fun getAllTasks(user: User): List<Task>
    suspend fun addTask(user: User, taskData: TaskData): Task
    fun taskEvents(): Flow<TaskEvent>
    suspend fun addCompletion(task: Task, completionData: CompletionData)
    suspend fun getAllCompletions(taskId: UUID): List<Completion>

}

sealed class TaskEvent {
    data class CompletionAdded(val completion: Completion) : TaskEvent()
    data class TaskAdded(val task: Task) : TaskEvent()
}
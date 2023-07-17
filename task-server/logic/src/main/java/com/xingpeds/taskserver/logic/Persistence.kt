package com.xingpeds.taskserver.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.flow.Flow

interface Persistence {
    suspend fun getAllTasks(user: User): List<Task>
    suspend fun addTask(user: User, taskData: TaskData): Task
    fun taskEvents(user: User): Flow<TaskEvent>
    suspend fun addCompletion(task: Task, completionData: CompletionData)
    suspend fun getAllCompletions(taskId: UUID): List<Completion>
    suspend fun addUser(userData: UserData): User?
    suspend fun doesUserExist(user: User): Boolean

}

sealed class TaskEvent {
    data class CompletionAdded(val completion: Completion) : TaskEvent()
    data class TaskAdded(val task: Task) : TaskEvent()
}
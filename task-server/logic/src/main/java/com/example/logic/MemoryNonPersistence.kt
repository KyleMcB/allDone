package com.example.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class MemoryNonPersistence(
    private val taskMap: MutableMap<User, List<Task>> = mutableMapOf(),
    private val completionMap: MutableMap<UUID, List<Completion>> = mutableMapOf()
) :
    Persistence {
    val events = MutableSharedFlow<TaskEvent>()
    override suspend fun getAllTasks(user: User): List<Task> {
        return taskMap[user] ?: emptyList()
    }

    override suspend fun addTask(user: User, taskData: TaskData): Task {
        val task = taskData.taskFromData(randUuid())
        taskMap[user] = (taskMap[user] ?: emptyList()) + task
        events.emit(TaskEvent.TaskAdded(task))
        return task
    }

    override fun taskEvents(): Flow<TaskEvent> {
        return events
    }

    override suspend fun addCompletion(task: Task, completionData: CompletionData) {
        val comp = Completion(task.id, randUuid(), completionData.timeStamp)
        completionMap[task.id] = (completionMap[task.id] ?: emptyList()) + comp
        events.emit(TaskEvent.CompletionAdded(comp))
    }
}

fun TaskData.taskFromData(id: UUID) = Task(name, id, type, notificationType, due)
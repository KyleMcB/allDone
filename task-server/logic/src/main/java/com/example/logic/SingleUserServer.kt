package com.example.logic

import com.xingpeds.alldone.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * This is a server that handles a single user.
 * It will connect multiple clients to the same user.
 */
class SingleUserServer(
    val user: User,
    private val data: Persistence,
    private val context: CoroutineContext = Dispatchers.Default,
) {
    private val mRequestsFromUser: MutableSharedFlow<ClientMessage> =
        MutableSharedFlow(extraBufferCapacity = 10)
    val requestsFromUser: SharedFlow<ClientMessage> = mRequestsFromUser
    private val scope = CoroutineScope(context)
    private val _connections: MutableStateFlow<Set<ClientConnection>> =
        MutableStateFlow(emptySet())
    val connections: StateFlow<Set<ClientConnection>> = _connections

    init {
        scope.launch {
            data.taskEvents().collect { event ->
                when (event) {
                    is TaskEvent.TaskAdded -> broadcast(AddTaskResponse(event.task))
                    is TaskEvent.CompletionAdded -> broadcast((CreateCompletionResponse(event.completion)))
                }
            }
        }
    }

    private suspend fun handleRequest(
        message: AuthenticatedClientMessage,
        connection: ClientConnection,
    ) {
        println("handleRequest: $message")
        when (message) {
            AllTasks -> connection.send(AllTasksResponse(data.getAllTasks(user)))
            is AddTask -> data.addTask(user, message.taskData)
            is CreateCompletion -> data.addCompletion(message.task, message.completionData)
            is AllCompletionsForTask -> connection.send(
                AllCompletionsForTaskResponse(
                    taskId = message.taskId,
                    completions = data.getAllCompletions(
                        message.taskId
                    )
                )
            )
        }
    }


    fun addConnection(connection: ClientConnection) = _connections.update { it + connection }.also {
        scope.launch {
            mRequestsFromUser.emitAll(connection.inbound)
        }
        connection.inbound.onEach { handleRequest(it, connection) }.launchIn(scope)
    }

    private suspend fun handleRequest(message: ClientMessage, connection: ClientConnection) {
        when (message) {
            is AuthenticatedClientMessage -> handleRequest(message, connection)
            else -> connection.send(InvalidRequest)
        }
    }

    fun broadcast(message: ServerMessage) = scope.launch {
        connections.value.forEach { it.send(message) }
    }


}
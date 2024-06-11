package com.tomorrow.kmmProjectStartup.data.utils

import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.wss
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

open class BaseWebSocketService(
    val clientProvider: () -> HttpClient,
    val json: Json
) {
    var webSocketSession: HashMap<String, DefaultClientWebSocketSession> = HashMap()
    suspend inline fun <reified Model> startListening(
        crossinline setMessage: (Result<Model>) -> Unit,
        baseUrl: String,
        path: String,
        port: Int,
    ) {
        try {
            if (webSocketSession[path]?.isActive == true)
                webSocketSession[path]?.close()
            clientProvider().wss(
                host = baseUrl,
                path = path,
                port = port
            ) {
                val incomingMessages = launch {
                    onReceive(setMessage)
                }
                webSocketSession[path] = this
                incomingMessages.join()
            }
        } catch (e: Throwable) {
            println("Error: $e")
            setMessage(Result.failure(e))
        }

    }

    open suspend fun stopListening(path: String) {
        webSocketSession[path]?.close()
    }

    suspend inline fun <reified Model> sendMessage(
        message: Model,
        baseUrl: String,
        path: String,
        port: Int,
    ) {
        try {
            if (webSocketSession[path]?.isActive == true)
                webSocketSession[path]?.sendSerialized(message)
            else
                clientProvider().wss(
                    host = baseUrl,
                    path = path,
                    port = port
                ) {
                    launch {
                        sendSerialized(message)
                    }
                }

        } catch (e: Throwable) {
            throw e
        }
    }

    suspend inline fun <reified Model> DefaultClientWebSocketSession.onReceive(setMessage: (Result<Model>) -> Unit) {
        try {
            for (message in incoming) {
                val incomingString = message as? Frame.Text ?: continue
                val s =
                    json.decodeFromString<Model>(incomingString.readText())
                setMessage(Result.success(s))
            }
        } catch (e: Throwable) {
            println("Errors: $e")
            setMessage(Result.failure(e))
        }
    }
}
package com.example.clientlib

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.lang.ref.WeakReference
import kotlin.math.pow

object NimbusWebSocket {
    private var socket: WebSocket? = null
    private var connected = false
    private var deviceId: String? = null
    private var contextRef: WeakReference<Context>? = null

    private val client = OkHttpClient()
    private var reconnectAttempts = 0
    private var lastMessageTime = System.currentTimeMillis()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private const val INITIAL_DELAY = 2000L
    private const val MAX_DELAY = 60000L
    private const val WATCHDOG_TIMEOUT = 30000L

    fun connect(id: String, context: Context) {
        if (connected) return
        deviceId = id
        contextRef = WeakReference(context.applicationContext) // Safe reference
        reconnectAttempts = 0
        tryConnect()
    }

    private fun tryConnect() {
        val request = Request.Builder()
            .url("ws://10.0.2.2:8090/connect?device_id=$deviceId")
            .build()

        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                connected = true
                reconnectAttempts = 0
                lastMessageTime = System.currentTimeMillis()
                startWatchdog()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                lastMessageTime = System.currentTimeMillis()
                contextRef?.get()?.let { ctx ->
                    MessageHandler.onMessageReceived(ctx, text)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                connected = false
                stopWatchdog()
                scheduleReconnect()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                connected = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                connected = false
                stopWatchdog()
            }
        })
    }

    private fun scheduleReconnect() {
        reconnectAttempts++
        val delay = (INITIAL_DELAY * 2.0.pow(reconnectAttempts.coerceAtMost(6))).toLong()
            .coerceAtMost(MAX_DELAY)
        scope.launch {
            delay(delay)
            tryConnect()
        }
    }

    private fun startWatchdog() {
        scope.launch {
            while (connected) {
                delay(WATCHDOG_TIMEOUT)
                if (System.currentTimeMillis() - lastMessageTime > WATCHDOG_TIMEOUT) {
                    forceReconnect()
                    break
                }
            }
        }
    }

    private fun stopWatchdog() {
        scope.coroutineContext.cancelChildren()
    }

    fun forceReconnect() {
        disconnect()
        scheduleReconnect()
    }

    fun disconnect() {
        stopWatchdog()
        socket?.cancel()
        socket = null
        connected = false
    }
}



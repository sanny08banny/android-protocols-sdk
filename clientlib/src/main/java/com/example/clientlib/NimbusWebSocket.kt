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
    private const val TAG = "NimbusWebSocket"

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

    private var listener: PushMessageListener? = null

    fun registerListener(pushListener: PushMessageListener) {
        listener = pushListener
    }

    fun unregisterListener() {
        listener = null
    }

    fun connect(id: String, context: Context) {
        if (connected) {
            Log.d(TAG, "Already connected, skipping connect()")
            return
        }
        Log.d(TAG, "Attempting to connect with ID: $id")
        deviceId = id
        contextRef = WeakReference(context.applicationContext)
        reconnectAttempts = 0
        tryConnect()
    }

    private fun tryConnect() {
        val url = "ws://172.104.9.226:8090/connect?device_id=$deviceId"
        Log.d(TAG, "Connecting to $url")

        val request = Request.Builder()
            .url(url)
            .build()

        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                connected = true
                reconnectAttempts = 0
                lastMessageTime = System.currentTimeMillis()
                Log.d(TAG, "WebSocket connected")
                startWatchdog()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                lastMessageTime = System.currentTimeMillis()
                Log.d(TAG, "Received message: $text")
                contextRef?.get()?.let { ctx ->
                    listener?.onMessageReceived(text)
                        ?: run {
                            Log.w("NimbusPush", "No listener registered; using default handler")
                            MessageHandler.onMessageReceived(ctx,text)
                        }
//                    MessageHandler.onMessageReceived(ctx, text)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                connected = false
                Log.e(TAG, "WebSocket connection failed: ${t.message}", t)
                stopWatchdog()
                scheduleReconnect()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: [$code] $reason")
                webSocket.close(1000, null)
                connected = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: [$code] $reason")
                connected = false
                stopWatchdog()
            }
        })
    }

    private fun scheduleReconnect() {
        reconnectAttempts++
        val delay = (INITIAL_DELAY * 2.0.pow(reconnectAttempts.coerceAtMost(6))).toLong()
            .coerceAtMost(MAX_DELAY)
        Log.d(TAG, "Scheduling reconnect in $delay ms (attempt $reconnectAttempts)")
        scope.launch {
            delay(delay)
            tryConnect()
        }
    }

    private fun startWatchdog() {
        Log.d(TAG, "Watchdog started")
        scope.launch {
            while (connected) {
                delay(WATCHDOG_TIMEOUT)
                if (System.currentTimeMillis() - lastMessageTime > WATCHDOG_TIMEOUT) {
                    Log.w(TAG, "Watchdog timeout – forcing reconnect")
                    forceReconnect()
                    break
                }
            }
        }
    }

    private fun stopWatchdog() {
        Log.d(TAG, "Watchdog stopped")
        scope.coroutineContext.cancelChildren()
    }

    fun forceReconnect() {
        Log.d(TAG, "Force reconnect triggered")
        disconnect()
        scheduleReconnect()
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        stopWatchdog()
        socket?.cancel()
        socket = null
        connected = false
    }
}



package com.example.clientlib

import android.content.Context
import android.util.Log
import okhttp3.*

object NimbusWebSocket {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect(deviceId: String, context: Context) {
        val url = "ws://10.0.2.2:8090/connect?device_id=$deviceId"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d("NimbusPush", "Connected")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d("NimbusPush", "Message: $text")
                MessageHandler.onMessageReceived(context, text)
                ws.send("ACK") // Send ACK
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e("NimbusPush", "Connection failed: ${t.message}")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d("NimbusPush", "Connection closed: $reason")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Client closed")
    }
}


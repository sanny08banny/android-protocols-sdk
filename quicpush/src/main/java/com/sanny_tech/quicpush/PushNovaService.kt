package com.sanny_tech.quicpush

import android.app.Service
import android.content.Intent
import android.util.Log
import java.io.*
import java.net.ServerSocket

class PushNovaService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val deviceId = intent?.getStringExtra("DEVICE_ID")
        if (deviceId == null) {
            Log.e("PushNova", "❌ Missing DEVICE_ID extra in intent")
            return START_NOT_STICKY
        }

        // ✅ Load native library
        try {
            System.loadLibrary("pushnova")  // Loads libpushnova.so
        } catch (e: UnsatisfiedLinkError) {
            Log.e("PushNova", "❌ Failed to load libpushnova.so", e)
            return START_NOT_STICKY
        }

        // ✅ Start native QUIC client
        Thread {
//            try {
//                PushNo.startClient("10.0.2.2:4242", deviceId) // adjust address
//                Log.i("PushNova", "✅ Native client started")
//            } catch (e: Exception) {
//                Log.e("PushNova", "❌ Error starting native client", e)
//            }
        }.start()

        // Start socket listener for receiving messages
        startSocketListener()

        return START_STICKY
    }

    private fun startSocketListener() {
        Thread {
            try {
                val server = ServerSocket(9090)
                Log.i("PushNovaService", "🟢 Socket listener on port 9090")
                val client = server.accept()
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                while (true) {
                    val msg = reader.readLine() ?: break
                    PushNovaClient.dispatchMessage(msg)
                }
                client.close()
                server.close()
            } catch (e: Exception) {
                Log.e("PushNovaService", "❌ Socket listener error", e)
            }
        }.start()
    }

    override fun onBind(intent: Intent?) = null
}




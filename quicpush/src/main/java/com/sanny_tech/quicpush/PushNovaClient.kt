package com.sanny_tech.quicpush

import android.content.Context
import android.content.Intent

object PushNovaClient {
    private var listener: PushMessageListener? = null

    fun init(context: Context, deviceId: String, listener: PushMessageListener) {
        this.listener = listener
        val intent = Intent(context, PushNovaService::class.java).apply {
            putExtra("DEVICE_ID", deviceId)
        }
        context.startService(intent)
    }

    internal fun dispatchMessage(message: String) {
        listener?.onPushMessageReceived(message)
    }
}

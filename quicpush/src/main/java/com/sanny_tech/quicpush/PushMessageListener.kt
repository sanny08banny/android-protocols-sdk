package com.sanny_tech.quicpush

interface PushMessageListener {
    fun onPushMessageReceived(message: String)
}

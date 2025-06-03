package com.nimbus.sdk

interface NimbusListener {
    fun onConnected()
    fun onMessageReceived(from: String, message: String)
    fun onError(e: Exception)
}

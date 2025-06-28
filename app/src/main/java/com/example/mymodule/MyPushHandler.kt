package com.example.mymodule

import android.util.Log
import com.example.clientlib.PushMessageListener

class MyPushHandler : PushMessageListener {
    override fun onMessageReceived(message: String) {
        Log.d("MyAppPush", "Custom message: $message")
        // Parse, display, store, or route message as needed
    }
}

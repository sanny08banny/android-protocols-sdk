package com.example.clientlib

import android.content.Context
import android.content.SharedPreferences

object NimbusPushClient {
    private const val PREFS = "nimbus_prefs"
    private const val DEVICE_KEY = "device_id"

    fun init(context: Context, deviceId: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(DEVICE_KEY, deviceId)
            .apply()
    }

    fun getDeviceId(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(DEVICE_KEY, "") ?: ""
    }
}

package com.example.mymodule

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clientlib.NimbusPushService
import com.example.clientlib.NimbusWebSocket


class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_POST_NOTIFICATIONS = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//        val client = QuicClient(this)
//        client.sendRequest(
//            "http://10.0.2.2:4242",
//            onResponse = { response -> Log.d("QUIC", "Received: $response") },
//            onError = { error -> Log.e("QUIC", "Failed", error) }
//        )
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
            }

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

//        val client = NimbusXMPPClient(
//            server = "10.0.2.2",
//            domain = "yourdomain.com",
//            username = "device123",
//            listener = object : NimbusListener {
//                override fun onConnected() {
//                    println("✅ Connected to XMPP")
//                }
//
//                override fun onMessageReceived(from: String, message: String) {
//                    println("📩 [$from] $message")
//                }
//
//                override fun onError(e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        )
//
//        client.connect()


        NimbusPushService.createNotificationChannel(this)
        NimbusPushService.start(this,"e5230340-2a46-4764-ab93-27d8a2d3de0a")
        NimbusWebSocket.registerListener(MyPushHandler())
    }
}
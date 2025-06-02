package com.example.clientlib

import android.content.Context
import org.chromium.net.CronetEngine
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import org.chromium.net.CronetException

import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors



class QuicClient(context: Context) {

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private val cronetEngine: CronetEngine = CronetEngine.Builder(context)
        .enableQuic(true)
        .enableHttp2(true)
        .build()

    fun sendRequest(
        url: String,
        onResponse: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val requestBuilder = cronetEngine.newUrlRequestBuilder(
            url,
            object : UrlRequest.Callback() {
                private val responseData = StringBuilder()

                override fun onRedirectReceived(
                    request: UrlRequest,
                    info: UrlResponseInfo,
                    newLocationUrl: String
                ) {
                    request.followRedirect()
                }

                override fun onResponseStarted(request: UrlRequest, info: UrlResponseInfo) {
                    request.read(ByteBuffer.allocateDirect(1024))
                }

                override fun onReadCompleted(
                    request: UrlRequest,
                    info: UrlResponseInfo,
                    byteBuffer: ByteBuffer
                ) {
                    byteBuffer.flip()
                    val bytes = ByteArray(byteBuffer.remaining())
                    byteBuffer.get(bytes)
                    responseData.append(String(bytes))
                    byteBuffer.clear()
                    request.read(byteBuffer)
                }

                override fun onSucceeded(request: UrlRequest, info: UrlResponseInfo) {
                    onResponse(responseData.toString())
                }

                override fun onFailed(
                    request: UrlRequest,
                    info: UrlResponseInfo?,
                    error: CronetException
                ) {
                    onError(error)
                }
            },
            executor
        )

        requestBuilder.build().start()
    }
}

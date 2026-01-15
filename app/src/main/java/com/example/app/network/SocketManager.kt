package com.example.app.network


import android.content.Context
import android.util.Log
import com.example.app.network.dto.auth.AuthManager
import com.example.app.utils.NotificationHelper
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import java.net.URISyntaxException

object SocketManager {
    private var mSocket: Socket? = null
    private const val SOCKET_URL = "https://nonoily-overinfluential-deegan.ngrok-free.dev"
    private val _notificationFlow = MutableSharedFlow<JSONObject>(extraBufferCapacity = 1)
    val notificationFlow = _notificationFlow.asSharedFlow()
    fun connect() {
        if (mSocket?.connected() == true) return

        val token = AuthManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            Log.e("SocketManager", "Không tìm thấy Token, không thể kết nối Socket")
            return
        }

        try {
            val options = IO.Options.builder()
                // Gửi token qua auth.token giống hệt middleware của Server yêu cầu
                .setAuth(mapOf("token" to token))
                .build()

            mSocket = IO.socket(SOCKET_URL, options)


            mSocket?.on(Socket.EVENT_CONNECT) {
                Log.d("SocketManager", "Kết nối Socket thành công!")
            }


            mSocket?.on("connected") { args ->
                val data = args[0] as JSONObject
                Log.d("SocketManager", "Server xác nhận: ${data.optString("message")}")
            }

            mSocket?.on("new_notification") { args ->
                val data = args[0] as JSONObject
                handleNotification(data)
            }

            mSocket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                Log.e("SocketManager", "Lỗi kết nối Socket: ${args[0]}")
            }

            mSocket?.connect()
        } catch (e: URISyntaxException) {
            Log.e("SocketManager", "Sai định dạng URL: ${e.message}")
        }
    }

    fun disconnect() {
        mSocket?.disconnect()
        mSocket?.off()
        mSocket = null
        Log.d("SocketManager", "Đã ngắt kết nối Socket")
    }
    private var appContext: Context? = null // Thêm biến này


    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun handleNotification(data: JSONObject) {
        try {
            // Lấy dữ liệu từ JSON gửi từ Node.js
            val message = data.optString("content", "Bạn có thông báo mới")
            val title = "Thông báo ứng dụng"
            // Sử dụng safe call để gọi helper
            _notificationFlow.tryEmit(data)
            appContext?.let { ctx ->
                NotificationHelper.showNotification(ctx, title, message)
            }

            Log.d("SocketManager", "Đã hiển thị thông báo: $message")
        } catch (e: Exception) {
            Log.e("SocketManager", "Lỗi xử lý JSON: ${e.message}")
        }
    }

}
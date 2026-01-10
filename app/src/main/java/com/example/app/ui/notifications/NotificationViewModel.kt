package com.example.app.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.network.SocketManager
import com.example.app.ui.notifications.model.AppNotification
import com.example.app.ui.notifications.model.NotificationType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class NotificationViewModel : ViewModel() {
    private val _allNotifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val allNotifications = _allNotifications.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    init {
        observeSocketNotifications()
        refreshNotifications()
    }

    private fun observeSocketNotifications() {
        viewModelScope.launch {
            SocketManager.notificationFlow.collect { json ->
                // SỬ DỤNG TẠI ĐÂY: Chuyển đổi JSON sang Object an toàn
                val newNotif = mapJsonToNotification(json)
                _allNotifications.update { currentList ->
                    listOf(newNotif) + currentList
                }
            }
        }
    }

    // ✅ HÀM SỬA LỖI NẰM Ở ĐÂY
    private fun mapJsonToNotification(json: JSONObject): AppNotification {
        // 1. Xử lý Enum an toàn: chuyển sang chữ hoa và bắt lỗi case-sensitive
        val typeString = json.optString("type", "").uppercase()
        val notificationType = try {
            NotificationType.valueOf(typeString)
        } catch (e: Exception) {
            NotificationType.SHARE // Giá trị dự phòng nếu server trả về type lạ
        }

        // 2. Khởi tạo Object khớp với Model (Đã thêm actorName)
        return AppNotification(
            id = json.optString("id", System.currentTimeMillis().toString()),
            type = notificationType,
            actorName = "Người dùng", // Bạn nên yêu cầu Server trả thêm field 'actor_name'
            message = json.optString("content", "Bạn có thông báo mới"), // Lấy từ key 'content'
            timeText = "Vừa xong",
            isRead = json.optBoolean("is_read", false),
            actorId = json.optString("actor_id"),
            postId = json.optString("target_id"),
            avatarUrl = null
        )
    }

    fun refreshNotifications() {
        viewModelScope.launch {
            _loading.value = true
            // Giả lập load dữ liệu từ API cũ ở đây
            _loading.value = false
        }
    }

    fun markAllRead() {
        _allNotifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }

    fun markAsRead(id: String) {
        _allNotifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }
}
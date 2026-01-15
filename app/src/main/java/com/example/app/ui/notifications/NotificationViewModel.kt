package com.example.app.ui.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.data.repository.NotificationRepository
import com.example.app.domain.model.Notification // Model từ Domain/Repo
import com.example.app.domain.model.AppNotification // Model của UI
import com.example.app.domain.model.NotificationType
import com.example.app.network.SocketManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class NotificationViewModel : ViewModel() {
    // 1. Khởi tạo Repository (Tốt nhất là nên dùng Hilt/Koin để inject, ở đây mình new trực tiếp cho đơn giản)
    private val repository = NotificationRepository()

    private val _allNotifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val allNotifications = _allNotifications.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    // State cho số lượng chưa đọc
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount = _unreadCount.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error = _error.asSharedFlow()

    init {
        observeSocketNotifications()
        fetchNotifications() // Gọi hàm lấy dữ liệu thật
        fetchUnreadCount()   // Gọi hàm lấy số lượng chưa đọc
    }

    // --- 3 HÀM GỌI REPO BẠN YÊU CẦU ---

    /**
     * 1. Lấy danh sách thông báo từ API
     */
    fun fetchNotifications() {
        viewModelScope.launch {
            _loading.value = true
            // Gọi Repo
            val result = repository.getNotifications(cursor = null, limit = 20)

            result.onSuccess { domainList ->
                // Map từ Domain Model (Notification) -> UI Model (AppNotification)
                val uiList = domainList.map { mapDomainToAppNotification(it) }
                _allNotifications.value = uiList
            }.onFailure { exception ->
                _error.emit(exception.message ?: "Lỗi tải thông báo")
            }
            _loading.value = false
        }
    }

    /**
     * 2. Đánh dấu đã đọc qua API
     */
    fun markAsRead(id: String) {
        viewModelScope.launch {
            // 1. Cập nhật UI ngay lập tức (Optimistic Update) để app mượt
            _allNotifications.update { list ->
                list.map { if (it.id == id) it.copy(isRead = true) else it }
            }
            // Giảm số lượng chưa đọc đi 1 nếu > 0
            if (_unreadCount.value > 0) {
                _unreadCount.value -= 1
            }

            // 2. Gọi API để đồng bộ với Server
            val result = repository.markAsRead(id)

            result.onFailure { e ->
                // Nếu lỗi, có thể revert lại UI hoặc chỉ log lỗi
                Log.e("NotiViewModel", "Mark read failed: ${e.message}")
            }
        }
    }

    /**
     * 3. Lấy số lượng tin chưa đọc
     */
    fun fetchUnreadCount() {
        viewModelScope.launch {
            val result = repository.getUnreadCount()

            result.onSuccess { count ->
                _unreadCount.value = count
            }.onFailure {
                Log.e("NotiViewModel", "Get unread count failed")
            }
        }
    }

    // --- CÁC HÀM HỖ TRỢ KHÁC ---

    // Hàm Map từ Domain Model sang UI Model
    // (Vì Repo trả về Notification nhưng ViewModel đang dùng AppNotification)
    private fun mapDomainToAppNotification(domain: Notification): AppNotification {
        // Mapping type string sang Enum
        val typeEnum = when (domain.type) {
            "like_post" -> NotificationType.LIKE_POST
            "comment_post" -> NotificationType.COMMENT_POST
            "share_post" -> NotificationType.SHARE_POST
            "follow" -> NotificationType.FOLLOW
            // Thêm các case khác tùy model của bạn
            else -> NotificationType.SHARE_POST
        }

        return AppNotification(
            id = domain.id,
            type = typeEnum,
            actorName = domain.users.username, // Giả sử model Notification có object users
            message = domain.content,
            timeText = domain.createdAt, // Bạn có thể dùng hàm formatTimeAgo ở đây
            isRead = domain.isRead,
            actorId = domain.users.id,
            postId = domain.targetId,
            avatarUrl = domain.users.avatar
        )
    }

    // Giữ nguyên logic Socket cũ của bạn
    private fun observeSocketNotifications() {
        viewModelScope.launch {
            SocketManager.notificationFlow.collect { json ->
                val newNotif = mapJsonToNotification(json)
                _allNotifications.update { currentList ->
                    listOf(newNotif) + currentList
                }
                _unreadCount.value += 1
            }
        }
    }

    // Giữ nguyên hàm map JSON cũ cho Socket
    private fun mapJsonToNotification(json: JSONObject): AppNotification {
        val rawType = json.optString("type", "").lowercase()
        val notificationType = when (rawType) {
            "like_post"      -> NotificationType.LIKE_POST
            "like_comment"   -> NotificationType.LIKE_COMMENT
            "comment_post"   -> NotificationType.COMMENT_POST
            "reply_comment"  -> NotificationType.REPLY_COMMENT
            "follow"         -> NotificationType.FOLLOW
            "share_post"     -> NotificationType.SHARE_POST
            else             -> NotificationType.SHARE_POST
        }

        return AppNotification(
            id = json.optString("id", System.currentTimeMillis().toString()),
            type = notificationType,
            actorName = "Ai đó", // Cần parse user name từ json nếu có
            message = json.optString("content", "đã tương tác với bạn"),
            timeText = "Vừa xong",
            isRead = json.optBoolean("is_read", false),
            actorId = json.optString("actor_id"),
            postId = json.optString("target_id"),
            avatarUrl = null // Parse avatar từ json nếu có
        )
    }

    fun markAllRead() {
        // Logic gọi API mark all read nếu có, tạm thời chỉ update UI
        _allNotifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
        _unreadCount.value = 0
    }
}
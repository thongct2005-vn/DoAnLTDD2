package com.example.app.ui.notifications.model

import java.util.UUID

enum class NotificationType { LIKE, COMMENT, FOLLOW, SHARE }

data class CommentSnippet(
    val authorName: String,
    val text: String
)

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType,
    val actorName: String,
    val message: String,
    val timeText: String,
    val isRead: Boolean = false,
    val avatarUrl: String? = null,
    val thumbUrl: String? = null,
    val postId: String? = null,
    val actorId: String? = null,

    // ✅ chỉ dùng cho COMMENT
    val commentPreview: CommentSnippet? = null, // comment gốc
    val replyPreview: CommentSnippet? = null    // trả lời cho comment đó
)

fun AppNotification.buildTitle(): String {
    return when (type) {
        NotificationType.LIKE -> "$actorName đã thích bài viết của bạn"
        NotificationType.FOLLOW -> "$actorName đã theo dõi bạn"
        NotificationType.SHARE -> "$actorName đã chia sẻ bài viết của bạn"
        NotificationType.COMMENT -> {
            if (replyPreview != null) "$actorName đã trả lời bình luận của bạn"
            else "$actorName đã bình luận vào bài viết của bạn"
        }
    }
}

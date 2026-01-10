package com.example.app.ui.notifications.data

import com.example.app.ui.notifications.model.AppNotification
import com.example.app.ui.notifications.model.CommentSnippet
import com.example.app.ui.notifications.model.NotificationType

object NotificationsFakeData {
    val demo = listOf(
        // ✅ LIKE
        AppNotification(
            type = NotificationType.LIKE,
            actorName = "Minh Anh",
            message = "“Bức ảnh này đẹp quá!”",
            timeText = "2 giờ",
            isRead = false,
            thumbUrl = "https://picsum.photos/200/200",
            postId = "post_1",
            actorId = "user_2"
        ),

        // ✅ COMMENT + REPLY (giống hình bạn)
        AppNotification(
            type = NotificationType.COMMENT,
            actorName = "Phương Phạm",
            message = "",
            timeText = "4 giờ",
            isRead = false,
            thumbUrl = "https://picsum.photos/200/201",
            postId = "post_2",
            actorId = "user_3",
            commentPreview = CommentSnippet(
                authorName = "Phương Phạm",
                text = "Hãy bạn cho PP 1 anon trong đời"
            ),
            replyPreview = CommentSnippet(
                authorName = "Ta Vy",
                text = "cook liền"
            )
        ),

        // ✅ SHARE
        AppNotification(
            type = NotificationType.SHARE,
            actorName = "Bảo Trân",
            message = "đã chia sẻ bài viết của bạn",
            timeText = "1 ngày",
            isRead = true,
            thumbUrl = "https://picsum.photos/200/202",
            postId = "post_3",
            actorId = "user_4"
        ),

        // ✅ FOLLOW
        AppNotification(
            type = NotificationType.FOLLOW,
            actorName = "Lan Phương",
            message = "Bây giờ hai bạn đã kết nối.",
            timeText = "3 ngày",
            isRead = true,
            actorId = "user_5"
        ),
        AppNotification(
        type = NotificationType.COMMENT,
        actorName = "Hoàng Long",
        message = "", // vì comment nằm trong preview
        timeText = "Hôm qua",
        isRead = false,
        thumbUrl = "https://picsum.photos/200/210",
        postId = "post_10",
        actorId = "user_10",
        commentPreview = CommentSnippet(
            authorName = "Hoàng Long",
            text = "Bài này hay quá, cho mình xin thêm thông tin với!"
        ),
        replyPreview = null // ✅ comment vào bài viết => không có reply
    )
    )
}

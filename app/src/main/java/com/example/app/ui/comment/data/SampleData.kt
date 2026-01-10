package com.example.app.ui.comment.data

import com.example.myapplication.domain.model.Comment
import com.example.myapplication.domain.model.CommentItem

fun sampleComments(): List<CommentItem> {
    return listOf(
        // ===== COMMENT CHA 1 =====
        CommentItem.Parent(
            comment = Comment(
                id = "c1",
                postId = "p1",
                userId = "u1",
                userName = "An",
                content = "Bài viết hay quá!",
                parentId = null,
                replyToUserName = null,
                createdAt = System.currentTimeMillis() - 3600000 // 1 giờ trước
            )
        ),
        CommentItem.Reply(
            comment = Comment(
                id = "r1",
                postId = "p1",
                userId = "u2",
                userName = "Bình",
                content = "Chuẩn luôn 👍",
                parentId = "c1",
                replyToUserName = "An",
                createdAt = System.currentTimeMillis() - 3000000
            ),
            replyToUserName = "An",
            level = 1
        ),
        CommentItem.Reply(
            comment = Comment(
                id = "r2",
                postId = "p1",
                userId = "u3",
                userName = "Chi",
                content = "Mình cũng thấy vậy",
                parentId = "c1",
                replyToUserName = "Bình",
                createdAt = System.currentTimeMillis() - 1800000
            ),
            replyToUserName = "Bình",
            level = 2
        ),

        // ===== COMMENT CHA 2 =====
        CommentItem.Parent(
            comment = Comment(
                id = "c2",
                postId = "p1",
                userId = "u4",
                userName = "Dũng",
                content = "Nội dung rất hữu ích",
                parentId = null,
                replyToUserName = null,
                createdAt = System.currentTimeMillis() - 7200000
            )
        ),
        CommentItem.Reply(
            comment = Comment(
                id = "r3",
                postId = "p1",
                userId = "u5",
                userName = "Hà",
                content = "Cảm ơn bạn chia sẻ",
                parentId = "c2",
                replyToUserName = "Dũng",
                createdAt = System.currentTimeMillis() - 6000000
            ),
            replyToUserName = "Dũng",
            level = 1
        ),

        // ===== COMMENT CHA 3 (KHÔNG CÓ REPLY) =====
        CommentItem.Parent(
            comment = Comment(
                id = "c3",
                postId = "p1",
                userId = "u6",
                userName = "Lan",
                content = "Có ai gặp lỗi giống mình không?",
                parentId = null,
                replyToUserName = null,
                createdAt = System.currentTimeMillis() - 86400000 // 1 ngày trước
            )
        )
    )
}
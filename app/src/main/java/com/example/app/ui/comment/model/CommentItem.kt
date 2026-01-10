package com.example.myapplication.domain.model


sealed class CommentItem {

    abstract val comment: Comment
    abstract val level: Int
    data class Parent(
        override val comment: Comment, // Dùng override
        override val level: Int = 0    // Parent mặc định là 0
    ) : CommentItem()

    data class Reply(
        override val comment: Comment,
        val replyToUserName: String,   // Thêm cái này để hiện @Tên_người_dùng
        override val level: Int        // Level tăng dần 1, 2, 3...
    ) : CommentItem()
}

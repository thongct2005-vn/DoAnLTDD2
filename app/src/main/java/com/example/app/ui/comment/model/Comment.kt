package com.example.myapplication.domain.model

import android.net.Uri

data class Comment(
    val id: String,
    val postId: String,
    val userId: String,
    val userName: String,
    val content: String,
    val likeCount: Int = 0,
    val parentId: String?,
    val replyToUserName: String?,
    val createdAt: Long,
    val imageUri: Uri? = null,
    val videoUrl: String? = null
)
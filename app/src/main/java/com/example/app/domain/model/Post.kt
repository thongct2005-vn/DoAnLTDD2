package com.example.app.domain.model


import androidx.room.*
import kotlinx.serialization.SerialName


data class Post(
    val id: String,
    val content: String,
    val media: List<Media>? = emptyList(),
    val privacy: String,
    val likeCount: Int,
    val commentCount: Int,
    val createdAt: String?,
    val user: User,
    val sharedPost: SharedPost? = null,
    val isLiked: Boolean = false
)

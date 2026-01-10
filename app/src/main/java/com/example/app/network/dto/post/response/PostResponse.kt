package com.example.app.network.dto.post.response
import com.example.app.domain.model.Media
import com.example.app.domain.model.SharedPost
import com.example.app.domain.model.User

import com.google.gson.annotations.SerializedName

data class PostResponse(
    val id: String,
    val content: String,
    val media: List<Media>? = null,
    val privacy: String,

    @SerializedName("like_count")
    val likeCount: Int,

    @SerializedName("comment_count")
    val commentCount: Int,

    @SerializedName("created_at")
    val createdAt: String?,

    val user: User,

    @SerializedName("shared_post")
    val sharedPost: SharedPost? = null,

    @SerializedName("is_liked")
    val isLiked: Boolean
)

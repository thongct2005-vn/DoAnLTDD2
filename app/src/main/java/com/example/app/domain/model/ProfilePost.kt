package com.example.app.domain.model

import com.example.app.network.dto.post.response.PostResponse
import kotlinx.serialization.SerialName

data class ProfilePost (
    val posts: List<Post>,
    val nextCursor: String? = null
)
package com.example.app.network.dto.search

data class SearchResponse(
    val success: Boolean,
    val data: List<SearchUserResponse>
)

data class SearchUserResponse(
    val id: String,
    val username: String,
    val avatar: String?,
    val is_me: Boolean,
    val is_follower: Boolean,
    val is_following: Boolean
)

fun SearchUserResponse.relationLabel(): String? {
    return when {
        is_me -> "Bạn"
        is_follower && is_following -> "Bạn bè"
        is_following -> "Đang theo dõi"
        is_follower -> "Theo dõi bạn"
        else -> null
    }
}

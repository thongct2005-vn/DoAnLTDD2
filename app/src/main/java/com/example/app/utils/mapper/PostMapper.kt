package com.example.app.utils.mapper

import android.util.Log
import com.example.app.domain.model.Post
import com.example.app.network.dto.post.response.PostResponse

/**
 * Map từ PostResponse (API) → Post (domain model UI hiện tại của bạn)
 * Giữ nguyên kiểu dữ liệu như trong Post.kt bạn đang dùng
 */
// package: com.example.app.utils.mapper (hoặc data/mapper)

fun PostResponse.toDomain(): Post {
    Log.d("MAPPER_CHECK", "Post ID: $id - isLiked from API: $isLiked")
   return Post(
        id = id,
        content = content,
        media = media,
        privacy = privacy,
        likeCount = likeCount,
        commentCount = commentCount,
        createdAt = createdAt,
        user = user,
        sharedPost = sharedPost,
        isLiked = isLiked
    )
}

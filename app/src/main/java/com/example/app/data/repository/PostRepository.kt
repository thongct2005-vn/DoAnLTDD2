package com.example.app.data.repository

import com.example.app.domain.model.Media
import com.example.app.network.RetrofitClient
import com.example.app.network.api.PostApiService
import com.example.app.network.dto.post.request.CreatePostRequest
import com.example.app.network.dto.post.request.SharePostRequest
import com.example.app.network.dto.post.response.LikeResponse
import com.example.app.network.dto.post.response.PostResponse
import com.example.app.network.dto.post.response.SharePostApiResponse
import com.example.app.network.dto.post.response.SharePostResponse
import retrofit2.Response

class PostRepository {
    private val api = RetrofitClient.create(PostApiService::class.java)

    // 1. Tạo bài post mới
    suspend fun createPost(
        content: String,
        privacy: String,
        mediaItems: List<Media>
    ): Response<PostResponse> {
        val request = CreatePostRequest(
            content = content,
            privacy = privacy.lowercase(),
            media = mediaItems.ifEmpty { null }
        )
        return api.createPost(request)
    }
    suspend fun toggleLike(postId: String, isCurrentlyLiked: Boolean): Result<String> {
        return try {
            val response = if (isCurrentlyLiked) api.unlikePost(postId) else api.likePost(postId)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.success == true) {
                    Result.success(body.message ?: "Success")
                } else {
                    Result.failure(Exception(body?.message ?: "API returned success=false"))
                }
            } else {
                // Optional: parse error body nếu server trả JSON error
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception("HTTP ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sharePost(
        originalPostId: String,
        content: String? = null,
        privacy: String = "public"
    ): Result<SharePostResponse?> {
        return try {
            val request = SharePostRequest(
                content = content,
                privacy = privacy
            )
            val response = api.sharePost(
                postId = originalPostId,
                request = request
            )

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        Result.success(body.sharedPost)
                    } else {
                        Result.failure(Exception("API trả về success = false"))
                    }
                } ?: Result.failure(Exception("Response body rỗng"))
            } else {
                when (response.code()) {
                    409 -> Result.success(null)
                    else -> Result.failure(Exception("Lỗi mạng: ${response.code()} - ${response.message()}"))
                }
            }
        }

        catch (e: Exception) {
            Result.failure(e)
        }
    }
}
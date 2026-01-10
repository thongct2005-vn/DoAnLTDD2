package com.example.app.network.api

import com.example.app.network.dto.follow.response.FollowResponse
import com.example.app.network.dto.post.request.CreatePostRequest
import com.example.app.network.dto.post.request.SharePostRequest
import com.example.app.network.dto.post.response.LikeResponse
import com.example.app.network.dto.post.response.PostResponse
import com.example.app.network.dto.post.response.SharePostApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path


interface PostApiService {
    @POST("api/posts/createPost")
    suspend fun createPost(@Body request: CreatePostRequest): Response<PostResponse>


    @POST("api/posts/{id}/like")
    suspend fun likePost(@Path("id") postId: String): Response<LikeResponse>

    @DELETE("api/posts/{id}/like")
    suspend fun unlikePost(@Path("id") postId: String): Response<LikeResponse>

    @POST("api/posts/{id}/sharePost")
    suspend fun sharePost(
        @Path("id") postId: String,
        @Body request: SharePostRequest
    ): Response<SharePostApiResponse>
}
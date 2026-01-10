package com.example.app.network.api

import com.example.app.network.dto.follow.response.FollowResponse
import com.example.app.network.dto.profile.response.ProfileDetailResponse
import com.example.app.network.dto.profile.response.ProfilePostResponse
import com.example.app.network.dto.profile.response.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProfileApiService {
    @GET("api/users/{id}/getProfile") // endpoint như backend của bạn
    suspend fun getProfile(@Path("id") id: String): Response<ProfileDetailResponse>

    @GET("api/users/{id}/getPostsProfile")
    suspend fun getProfilePosts(
        @Path("id") userId: String,
        @Query("limit") limit: Int = 5,
        @Query("cursor") cursor: String? = null

    ): Response<ProfilePostResponse>

    @POST("api/users/{id}/follow")
    suspend fun followUser(@Path("id") userId: String): Response<FollowResponse>

    @DELETE("api/users/{id}/follow")
    suspend fun unFollowUser(@Path("id") userId: String): Response<FollowResponse>

    @PATCH("api/users/updateProfile")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): Response<Unit>

}
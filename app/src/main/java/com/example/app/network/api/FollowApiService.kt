package com.example.app.network.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response
import com.example.app.network.dto.follow.response.FollowResponse

interface UserApiService {
    @GET("api/users/{id}/following")
    suspend fun getFollowing(
        @Path("id") userId: String,
        @Query("cursor") cursor: String?
    ): Response<FollowResponse>

    @GET("api/users/{id}/followers")
    suspend fun getFollowers(
        @Path("id") userId: String,
        @Query("cursor") cursor: String?
    ): Response<FollowResponse>
}
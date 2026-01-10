package com.example.app.network.api

import com.example.app.network.dto.search.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {

    @GET("api/users/search")
    suspend fun searchUsers(
        @Query("username") username: String
    ): Response<SearchResponse>
}

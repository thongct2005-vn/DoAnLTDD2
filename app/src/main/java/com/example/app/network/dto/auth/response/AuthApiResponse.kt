package com.example.app.network.dto.auth.response



data class AuthApiResponse(
    val success: Boolean,
    val message: String? = null,
    val code: String? = null,


    val user_id: String? = null,
    val email: String? = null,
    val access_token: String? = null,
    val refresh_token: String? = null,
    val isFirstLogin: Boolean? = false
)
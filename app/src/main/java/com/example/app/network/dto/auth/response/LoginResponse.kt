package com.example.app.network.dto.auth.response



data class LoginResponse(
    val access_token: String,
    val refresh_token: String,
    val isFirstLogin: Boolean
)

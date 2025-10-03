package com.example.adminapplication.data.model

data class LoginResponse(
    val token: String,
    val user: User
)
package com.example.myapplication.data.model

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String,
    val role: String
)
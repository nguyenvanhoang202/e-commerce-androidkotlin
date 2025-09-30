package com.example.myapplication.data.model

// ChangePasswordRequest.kt
data class ChangePasswordRequest(
    val username: String,
    val oldPassword: String,
    val newPassword: String
)


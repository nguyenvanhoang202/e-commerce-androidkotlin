package com.example.adminapplication.data.model

// ChangePasswordRequest.kt
data class ChangePasswordRequest(
    val username: String,
    val oldPassword: String,
    val newPassword: String
)


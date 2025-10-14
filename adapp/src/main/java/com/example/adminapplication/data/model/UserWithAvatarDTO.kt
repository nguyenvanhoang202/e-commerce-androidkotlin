package com.example.adminapplication.data.model

data class UserWithAvatarDTO(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val active: Boolean,
    val createdAt: String,
    val avatar: String?
)
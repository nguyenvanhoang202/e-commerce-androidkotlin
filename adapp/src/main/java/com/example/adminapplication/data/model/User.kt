package com.example.adminapplication.data.model

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val active: Boolean,
    val createdAt: String
)
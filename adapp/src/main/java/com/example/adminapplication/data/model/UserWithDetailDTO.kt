package com.example.adminapplication.data.model

data class UserWithDetailDTO(
    val username: String,
    val email: String,
    val role: String,
    val active: Boolean,
    val createdAt: String,
    var fullName: String,
    var phone: String,
    var address: String,
    var birthday: String,
    var gender: String,
    val avatar: String
)

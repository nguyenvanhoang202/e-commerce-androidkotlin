package com.example.adminapplication.data.model

import java.io.Serializable

data class CategoryRequest(
    val name: String?,
    val slug: String?,
    val description: String?
) : Serializable
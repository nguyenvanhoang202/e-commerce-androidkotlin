package com.example.adminapplication.data.model

import java.io.Serializable

data class ProductImage (
    val id: Long,
    val productId: Product,
    val imageUrl: String
): Serializable
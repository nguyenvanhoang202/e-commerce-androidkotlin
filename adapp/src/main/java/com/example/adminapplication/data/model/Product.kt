package com.example.adminapplication.data.model

import java.io.Serializable

data class Product(
    val id: Long?,
    val name: String?,
    val slug: String?,
    val price: Double?,
    val discountprice: Double?,
    val brand: String?,
    val imageUrl: String?,
    val description: String?,
    val stockquantity: Int?,
    val isNew: Boolean?,
    val isHot: Boolean?,
    val createdAt: String?,
    val category: Category?
): Serializable

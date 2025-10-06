package com.example.adminapplication.data.model

data class ApiResponse<T>(
    val success: Boolean,          // "success" hoặc "error"
    val message: String?,        // mô tả ngắn gọn
    val data: T?                 // dữ liệu trả về (List<Product>, Product, v.v.)
)

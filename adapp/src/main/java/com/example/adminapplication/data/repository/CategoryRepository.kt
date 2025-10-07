package com.example.adminapplication.data.repository

import com.example.adminapplication.data.api.ApiService
import com.example.adminapplication.data.model.ApiResponse
import com.example.adminapplication.data.model.Category
import retrofit2.Response

class CategoryRepository(private val apiService: ApiService) {
    suspend fun getAllCategories(): Response<ApiResponse<List<Category>>> {
        return apiService.getAllCategories()
    }
}
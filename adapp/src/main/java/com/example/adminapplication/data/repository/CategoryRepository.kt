package com.example.adminapplication.data.repository

import com.example.adminapplication.data.api.ApiService
import com.example.adminapplication.data.model.ApiResponse
import com.example.adminapplication.data.model.Category
import com.example.adminapplication.data.model.CategoryRequest
import retrofit2.Response

class CategoryRepository(private val apiService: ApiService) {

    suspend fun getAllCategories(): Response<ApiResponse<List<Category>>> {
        return apiService.getAllCategories()
    }
    suspend fun createCategory(name: String, slug: String, description: String?): Response<ApiResponse<Category>> {
        val categoryRequest = CategoryRequest(name = name, slug = slug, description = description)
        return apiService.createCategory(categoryRequest)
    }

    suspend fun updateCategory(id: Long?, name: String, slug: String, description: String): Response<ApiResponse<Category>> {
        val category = Category(id = id, name = name, slug = slug, description = description)
        return apiService.updateCategory(id, category)
    }
    suspend fun deleteCategory(id: Long?): Response<ApiResponse<Void>> {
        return apiService.deleteCategory(id ?: 0L)
    }
}

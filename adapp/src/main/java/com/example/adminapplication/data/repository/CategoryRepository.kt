package com.example.adminapplication.data.repository

import com.example.adminapplication.data.api.ApiService
import com.example.adminapplication.data.model.ApiResponse
import com.example.adminapplication.data.model.Category
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class CategoryRepository(private val apiService: ApiService) {

    suspend fun getAllCategories(): Response<ApiResponse<List<Category>>> {
        return apiService.getAllCategories()
    }

    suspend fun updateCategory(id: Long?, name: String, slug: String, description: String): Response<ApiResponse<Category>> {
        val nameBody = RequestBody.create("text/plain".toMediaTypeOrNull(), name)
        val slugBody = RequestBody.create("text/plain".toMediaTypeOrNull(), slug)
        val descBody = RequestBody.create("text/plain".toMediaTypeOrNull(), description)
        return apiService.updateCategory(id ?: 0L, nameBody, slugBody, descBody)
    }

    suspend fun deleteCategory(id: Long?): Response<ApiResponse<Void>> {
        return apiService.deleteCategory(id ?: 0L)
    }
}

package com.example.adminapplication.data.repository

import com.example.adminapplication.data.api.ApiService
import com.example.adminapplication.data.model.ApiResponse
import com.example.adminapplication.data.model.ProductImage
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

class ProductImageRepository(private val apiService: ApiService) {

    suspend fun getAllProductImages(productId: Long): Response<List<ProductImage>> {
        return apiService.getProductImages(productId)
    }

    suspend fun uploadImages(productId: Long, files: List<MultipartBody.Part>): Response<List<ProductImage>> {
        return apiService.uploadProductImages(productId, files)
    }

    suspend fun deleteImages(imageIds: List<Long>): Response<ApiResponse<Unit>> {
        return apiService.deleteProductImages(imageIds)
    }

    suspend fun updateImages(imageIds: List<Long>, files: List<MultipartBody.Part>): Response<ApiResponse<Any>> {
        return apiService.updateProductImages(files, imageIds)
    }

}

package com.example.adminapplication.data.repository

import com.example.adminapplication.data.api.ApiService
import com.example.adminapplication.data.model.ApiResponse
import com.example.adminapplication.data.model.Category
import com.example.adminapplication.data.model.Product
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class ProductRepository(private val apiService: ApiService) {

    // ---------- GET ALL ----------
    suspend fun getAllProducts(): Response<ApiResponse<List<Product>>> {
        return apiService.getAllProducts()
    }

    // ---------- CREATE PRODUCT ----------
    suspend fun createProductWithImage(
        name: RequestBody,
        slug: RequestBody,
        price: RequestBody,
        discountPrice: RequestBody,
        brand: RequestBody,
        stock: RequestBody,
        isNew: RequestBody,
        isHot: RequestBody,
        description: RequestBody,
        categoryId: RequestBody,
        file: MultipartBody.Part
    ): Response<ApiResponse<Product>> {
        return apiService.createProductWithImage(
            name, slug, price, discountPrice, brand, stock, isNew, isHot,
            description, categoryId, file
        )
    }

    // ---------- UPDATE PRODUCT ----------
    suspend fun updateProductWithImage(
        id: Long,
        name: RequestBody,
        slug: RequestBody,
        price: RequestBody,
        discountPrice: RequestBody,
        brand: RequestBody,
        stock: RequestBody,
        isNew: RequestBody,
        isHot: RequestBody,
        description: RequestBody,
        categoryId: RequestBody,
        file: MultipartBody.Part? = null // Optional: nếu không đổi ảnh
    ): Response<ApiResponse<Product>> {
        return apiService.updateProductWithImage(
            id, name, slug, price, discountPrice, brand, stock, isNew, isHot,
            description, categoryId, file
        )
    }

    // ---------- DELETE PRODUCT ----------
    suspend fun deleteProduct(id: Long): Response<ApiResponse<Void>> {
        return apiService.deleteProduct(id)
    }

//     ---------- GET PRODUCT BY ID ----------
    suspend fun getProductById(id: Long): Response<ApiResponse<Product>> {
        return apiService.getProductById(id)
    }
    suspend fun getAllCategories(): Response<ApiResponse<List<Category>>> {
        return apiService.getAllCategories()
    }
}

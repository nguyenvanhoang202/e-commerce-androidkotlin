package com.example.adminapplication.data.api

import com.example.adminapplication.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ---------- AUTH ----------
    @POST("/api/login")
    fun login(
        @Query("appType") appType: String,
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("/api/password/change")
    fun changePassword(
        @Body request: ChangePasswordRequest
    ): Call<ChangePasswordResponse>


    // ---------- PRODUCT ----------
    @GET("/api/product")
    suspend fun getAllProducts(): Response<ApiResponse<List<Product>>>

    // GET product by ID
    @GET("/api/product/{id}")
    suspend fun getProductById(
        @Path("id") id: Long
    ): Response<ApiResponse<Product>>

    //UDATE
    @Multipart
    @PUT("/api/product/{id}/update-with-image")
    suspend fun updateProductWithImage(
        @Path("id") id: Long,
        @Part("name") name: RequestBody,
        @Part("slug") slug: RequestBody,
        @Part("price") price: RequestBody,
        @Part("discountprice") discountPrice: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("stockquantity") stock: RequestBody,
        @Part("isNew") isNew: RequestBody,
        @Part("isHot") isHot: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category") category: RequestBody,
        @Part file: MultipartBody.Part? // Optional, có thể null nếu không đổi ảnh
    ): Response<ApiResponse<Product>>

    //CREATE
    @Multipart
    @POST("/api/product/create-with-image")
    suspend fun createProductWithImage(
        @Part("name") name: RequestBody,
        @Part("slug") slug: RequestBody,
        @Part("price") price: RequestBody,
        @Part("discountprice") discountPrice: RequestBody,
        @Part("brand") brand: RequestBody,
        @Part("stockquantity") stock: RequestBody,
        @Part("isNew") isNew: RequestBody,
        @Part("isHot") isHot: RequestBody,
        @Part("description") description: RequestBody,
        @Part("category") category: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<Product>>

    @DELETE("/api/product/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Long
    ): Response<ApiResponse<Void>>

    // ---------- CATEGORY ----------
    @GET("/api/category")
    suspend fun getAllCategories(): Response<ApiResponse<List<Category>>>

        // Thêm nhiều ảnh cho 1 product
        @Multipart
        @POST("/api/product-images/{productId}/upload")
        suspend fun uploadProductImages(
            @Path("productId") productId: Long,
            @Part files: List<MultipartBody.Part>
        ): Response<List<ProductImage>>

        // Lấy tất cả ảnh theo productId
        @GET("/api/product-images/{productId}")
        suspend fun getProductImages(
            @Path("productId") productId: Long
        ): Response<List<ProductImage>>

        // Xóa nhiều ảnh theo danh sách id
        @HTTP(method = "DELETE", path = "/api/product-images/delete", hasBody = true)
        suspend fun deleteProductImages(
            @Body imageIds: List<Long>
        ): Response<ApiResponse<Unit>>

        // Cập nhật nhiều ảnh theo danh sách id
        @Multipart
        @POST("/api/product-images/update")
        suspend fun updateProductImages(
            @Part files: List<MultipartBody.Part>,
            @Part("imageIds") imageIds: List<Long>
        ): Response<ApiResponse<Any>>
    }





package com.example.adminapplication.data.api

import com.example.adminapplication.data.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/api/login")
    fun login(@Query("appType") appType: String, @Body request: LoginRequest): Call<LoginResponse>

}
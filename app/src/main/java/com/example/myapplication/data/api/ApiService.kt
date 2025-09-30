    package com.example.myapplication.data.api

    import com.example.myapplication.data.model.*
    import retrofit2.Call
    import retrofit2.http.Body
    import retrofit2.http.POST

    interface ApiService {
        @POST("/api/login")
        fun login(@Body request: LoginRequest): Call<LoginResponse>

        @POST("/api/register")
        fun register(@Body request: RegisterRequest): Call<User>

        @POST("/api/password/change")
        fun changePassword(@Body request: ChangePasswordRequest): Call<ChangePasswordResponse>
    }
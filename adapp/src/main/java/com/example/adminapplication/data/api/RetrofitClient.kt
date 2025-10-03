package com.example.adminapplication.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/" // nhớ có dấu / ở cuối

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // log request & response
        .connectTimeout(30, TimeUnit.SECONDS) // timeout kết nối
        .readTimeout(30, TimeUnit.SECONDS)    // timeout đọc
        .writeTimeout(30, TimeUnit.SECONDS)   // timeout ghi
        .build()

    val instance: com.example.adminapplication.data.api.ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(com.example.adminapplication.data.api.ApiService::class.java)
    }
}
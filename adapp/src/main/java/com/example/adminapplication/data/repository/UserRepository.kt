package com.example.adminapplication.data.repository

import com.example.adminapplication.data.api.ApiService
import com.example.adminapplication.data.model.ApiResponse
import com.example.adminapplication.data.model.User
import com.example.adminapplication.data.model.UserWithAvatarDTO
import com.example.adminapplication.data.model.UserWithDetailDTO

class UserRepository(private val api: ApiService) {

    suspend fun getAllUsersForAdmin(): ApiResponse<List<UserWithAvatarDTO>> {
        val resp = api.getAllUsersForAdmin()
        return if (resp.isSuccessful) resp.body() ?: ApiResponse(false, "Empty response", null)
        else ApiResponse(false, "HTTP ${resp.code()}: ${resp.message()}", null)
    }

    suspend fun getAllUsersForManage(): ApiResponse<List<UserWithAvatarDTO>> {
        val resp = api.getAllUsersForManage()
        return if (resp.isSuccessful) resp.body() ?: ApiResponse(false, "Empty response", null)
        else ApiResponse(false, "HTTP ${resp.code()}: ${resp.message()}", null)
    }


    suspend fun updateUser(user: User): ApiResponse<User> {
        return api.updateUser(user.id, user)
    }

    suspend fun deleteUser(id: Long): ApiResponse<Any> {
        return api.deleteUser(id)
    }

    suspend fun updateUserActive(id: Long, active: Boolean): ApiResponse<User> {
        return api.updateUserActive(id, active)
    }
    suspend fun getUserDetailById(id: Long): ApiResponse<UserWithDetailDTO> {
        return api.getUserDetailById(id)
    }
    suspend fun updateUserDetail(id: Long, user: MutableMap<String, Any>): ApiResponse<UserWithDetailDTO> {
        return api.updateUserDetail(id, user)
    }
}

package com.example.adminapplication.ui.profile

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adminapplication.data.api.RetrofitClient
import com.example.adminapplication.data.model.ChangePasswordRequest
import com.example.adminapplication.data.model.ChangePasswordResponse
import com.example.adminapplication.databinding.ActivityChangePasswordBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val username = prefs.getString("username", null)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnChangePassword.setOnClickListener {
            val oldPassword = binding.oldPassword.text.toString().trim()
            val newPassword = binding.newPassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (username == null) {
                Toast.makeText(this, "Không tìm thấy username", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = ChangePasswordRequest(username, oldPassword, newPassword)

            RetrofitClient.instance.changePassword(request)
                .enqueue(object : Callback<ChangePasswordResponse> {
                    override fun onResponse(
                        call: Call<ChangePasswordResponse>,
                        response: Response<ChangePasswordResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@ChangePasswordActivity, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@ChangePasswordActivity, "Lỗi: ${response.body()?.message ?: response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                        Toast.makeText(this@ChangePasswordActivity, "Lỗi kết nối: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}

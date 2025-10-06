package com.example.adminapplication.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.adminapplication.R
import com.example.adminapplication.ui.auth.LoginActivity
import com.example.adminapplication.ui.profile.ChangePasswordActivity

class ProfileFragment : Fragment() {

    private lateinit var username: TextView
    private lateinit var itemChangePassword: Button
    private lateinit var itemLogout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        username = view.findViewById(R.id.username)
        itemChangePassword = view.findViewById(R.id.item_change_password)
        itemLogout = view.findViewById(R.id.item_logout)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔹 Lấy thông tin tài khoản từ SharedPreferences
        val prefs = requireContext().getSharedPreferences("AdminPrefs", Context.MODE_PRIVATE)
        val user = prefs.getString("username", "Admin")

        username.text = user

        // 🔹 Đổi mật khẩu
        itemChangePassword.setOnClickListener {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        // 🔹 Đăng xuất
        itemLogout.setOnClickListener {
            prefs.edit().clear().apply()
            Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}

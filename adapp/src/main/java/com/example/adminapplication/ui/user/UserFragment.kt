package com.example.adminapplication.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adminapplication.R
import com.example.adminapplication.data.api.RetrofitClient
import com.example.adminapplication.data.model.UserWithAvatarDTO
import com.example.adminapplication.data.repository.UserRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddUser: FloatingActionButton
    private lateinit var adapter: UserAdapter
    private val userRepository = UserRepository(RetrofitClient.instance)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewUsers)
        fabAddUser = view.findViewById(R.id.fabAddUser)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = UserAdapter(
            mutableListOf(),
            onEditClick = { user -> editUser(user) },
            onDeleteClick = { user -> deleteUser(user.id) },
            onActiveToggle = { user, isActive -> toggleActive(user, isActive) }
        )
        recyclerView.adapter = adapter

        fabAddUser.setOnClickListener {
            Toast.makeText(requireContext(), "Thêm user (chưa làm form)", Toast.LENGTH_SHORT).show()
        }

        parentFragmentManager.setFragmentResultListener("user_updated", viewLifecycleOwner) { _, _ ->
            loadUsers()
        }

        // 👉 Lắng nghe tín hiệu để ẩn/hiện nút Add từ UserInforFragment
        parentFragmentManager.setFragmentResultListener("toggle_add_button", viewLifecycleOwner) { _, bundle ->
            val show = bundle.getBoolean("show", true)
            fabAddUser.visibility = if (show) View.VISIBLE else View.GONE
        }

        loadUsers()
        return view
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    userRepository.getAllUsersForAdmin()
                }

                if (response.success && response.data != null) {
                    @Suppress("UNCHECKED_CAST")
                    val users = response.data as List<UserWithAvatarDTO>
                    val sortedUsers = users.sortedBy { it.id }
                    adapter.updateData(sortedUsers)
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteUser(userId: Long) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    userRepository.deleteUser(userId)
                }
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                if (response.success) loadUsers()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi xóa: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editUser(user: UserWithAvatarDTO) {
        // Ẩn nút add khi chuyển sang UserInforFragment
        parentFragmentManager.setFragmentResult("toggle_add_button", Bundle().apply {
            putBoolean("show", false)
        })

        val fragment = UserInforFragment.newInstance(user.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.userFragmentRoot, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun toggleActive(user: UserWithAvatarDTO, isActive: Boolean) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    userRepository.updateUserActive(user.id, isActive)
                }

                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()

                if (response.success) {
                    loadUsers()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi cập nhật: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }
}

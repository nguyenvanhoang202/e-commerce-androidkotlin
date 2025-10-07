package com.example.adminapplication.ui.category

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
import com.example.adminapplication.data.api.ApiService
import com.example.adminapplication.data.model.Category
import com.example.adminapplication.data.repository.CategoryRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddCategory: FloatingActionButton
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<Category>()
    private val repository = CategoryRepository(RetrofitClient.instance)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewCategories)
        fabAddCategory = view.findViewById(R.id.fabAddCategory)

        adapter = CategoryAdapter(
            categories,
            onEdit = { category ->
                Toast.makeText(requireContext(), "Sửa: ${category.name}", Toast.LENGTH_SHORT).show()
            },
            onDelete = { category ->
                Toast.makeText(requireContext(), "Xóa: ${category.name}", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        fabAddCategory.setOnClickListener {
            Toast.makeText(requireContext(), "Thêm danh mục mới", Toast.LENGTH_SHORT).show()
        }

        loadCategories()

        return view
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val response = repository.getAllCategories()
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        categories.clear()
                        categories.addAll(body.data)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), "Không có danh mục nào", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi tải danh mục: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

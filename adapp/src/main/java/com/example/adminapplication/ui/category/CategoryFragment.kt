package com.example.adminapplication.ui.category

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminapplication.R
import com.example.adminapplication.data.api.RetrofitClient
import com.example.adminapplication.data.repository.CategoryRepository
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private lateinit var adapter: CategoryAdapter
    private val repository = CategoryRepository(RetrofitClient.instance)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category, container, false)
        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerViewCategories)

        adapter = CategoryAdapter(mutableListOf(),
            onEditSave = { updated ->
                lifecycleScope.launch {
                    val res = repository.updateCategory(updated.id,
                        updated.name.toString(), updated.slug.toString(), updated.description ?: "")
                    if (res.isSuccessful && res.body()?.success == true) {
                        Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                        // cập nhật nội bộ adapter nhanh (không cần reload toàn bộ)
                        adapter.updateItem(res.body()!!.data ?: updated)
                    } else {
                        val err = res.errorBody()?.string()
                        Toast.makeText(requireContext(), "Lỗi khi cập nhật: ${res.code()} ${err ?: ""}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onDelete = { category ->
                lifecycleScope.launch {
                    val res = repository.deleteCategory(category.id)
                    if (res.isSuccessful && res.body()?.success == true) {
                        Toast.makeText(requireContext(), "Đã xóa danh mục", Toast.LENGTH_SHORT).show()
                        adapter.removeItemById(category.id)
                    } else {
                        val err = res.errorBody()?.string()
                        Toast.makeText(requireContext(), "Xóa thất bại: ${res.code()} ${err ?: ""}", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadCategories()

        return view
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val response = repository.getAllCategories()
                if (response.isSuccessful && response.body()?.success == true) {
                    val list = response.body()?.data ?: emptyList()
                    adapter.setData(list)
                } else {
                    val err = response.errorBody()?.string()
                    Toast.makeText(requireContext(), "Lỗi tải danh mục: ${response.code()} ${err ?: ""}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.example.adminapplication.ui.product

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.adminapplication.R
import com.example.adminapplication.data.api.RetrofitClient
import com.example.adminapplication.data.model.Product
import com.example.adminapplication.data.repository.ProductRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductFragment : Fragment() {

    private lateinit var productContainer: LinearLayout
    private lateinit var addButton: FloatingActionButton
    private lateinit var repository: ProductRepository

    private val baseUrl = "http://10.0.2.2:8080" // base URL backend

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product, container, false)
        productContainer = view.findViewById(R.id.product_container)
        addButton = view.findViewById(R.id.btn_add_product)

        repository = ProductRepository(RetrofitClient.instance)

        // Gọi API để hiển thị danh sách sản phẩm
        fetchAllProducts()

        // Lắng nghe sự kiện reload danh sách
        parentFragmentManager.setFragmentResultListener("product_added", viewLifecycleOwner) { key, bundle ->
            val success = bundle.getBoolean("success", false)
            if (success) fetchAllProducts()
        }

        addButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(this.id, AddProductFragment()) // Thay bằng ID của container hiện tại
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun fetchAllProducts() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.getAllProducts()
                }

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        val products = body.data ?: emptyList()
                        Log.d("ProductFragment", "products size: ${products.size}")
                        displayProducts(products)
                    } else {
                        Toast.makeText(requireContext(), "Lỗi: ${body?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi kết nối: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayProducts(products: List<Product>) {
        productContainer.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        for (product in products) {
            val itemView = inflater.inflate(R.layout.item_product_simple, productContainer, false)

            val imageView = itemView.findViewById<ImageView>(R.id.productImage)
            val nameView = itemView.findViewById<TextView>(R.id.product_name)
            val priceView = itemView.findViewById<TextView>(R.id.product_price)
            val btnEdit = itemView.findViewById<ImageView>(R.id.btn_edit)
            val btnDelete = itemView.findViewById<ImageView>(R.id.btn_delete)

            nameView.text = product.name ?: "Không có tên"
            priceView.text = "${product.price ?: 0}đ"

            Glide.with(requireContext())
                .load(baseUrl + (product.imageUrl ?: ""))
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(imageView)

            btnEdit.setOnClickListener {
                val bundle = Bundle().apply {
                    putLong("productId", product.id ?: 0L)
                    // Nếu muốn truyền toàn bộ object Product, cần Product implements Parcelable hoặc Serializable
                    putSerializable("product", product)
                }
                parentFragmentManager.beginTransaction()
                    .replace(this.id, UpdateProductFragment().apply { arguments = bundle })
                    .addToBackStack(null)
                    .commit()
            }

            btnDelete.setOnClickListener {
                val id = product.id ?: return@setOnClickListener
                deleteProduct(id)
            }

            productContainer.addView(itemView)
        }
    }

    private fun deleteProduct(id: Long) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    repository.deleteProduct(id)
                }

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show()
                    fetchAllProducts() // Load lại danh sách
                } else {
                    Toast.makeText(requireContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

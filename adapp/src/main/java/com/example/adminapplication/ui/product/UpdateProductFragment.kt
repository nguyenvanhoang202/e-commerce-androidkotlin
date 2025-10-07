package com.example.adminapplication.ui.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.adminapplication.R
import com.example.adminapplication.data.api.RetrofitClient
import com.example.adminapplication.data.model.Category
import com.example.adminapplication.data.model.Product
import com.example.adminapplication.data.repository.CategoryRepository
import com.example.adminapplication.data.repository.ProductRepository
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UpdateProductFragment : Fragment() {

    private lateinit var etName: TextInputEditText
    private lateinit var etSlug: TextInputEditText
    private lateinit var etPrice: TextInputEditText
    private lateinit var etDiscountPrice: TextInputEditText
    private lateinit var etBrand: TextInputEditText
    private lateinit var etStock: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var cbIsNew: CheckBox
    private lateinit var cbIsHot: CheckBox
    private lateinit var spinnerCategory: Spinner
    private lateinit var ivImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnUpdate: Button

    private var selectedImageUri: Uri? = null
    private var productId: Long = 0 // Set từ argument hoặc bundle
    private lateinit var repository: ProductRepository
    private lateinit var categoryRepository: CategoryRepository
    private var categories: List<Category> = emptyList()

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_update_product, container, false)

        // Nút back
        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val product = arguments?.getSerializable("product") as? Product
        product?.let { productId = it.id ?: 0L }

        // Bind views
        etName = view.findViewById(R.id.et_product_name)
        etSlug = view.findViewById(R.id.et_product_slug)
        etPrice = view.findViewById(R.id.et_product_price)
        etDiscountPrice = view.findViewById(R.id.et_product_discount_price)
        etBrand = view.findViewById(R.id.et_product_brand)
        etStock = view.findViewById(R.id.et_product_stock)
        etDescription = view.findViewById(R.id.et_product_description)
        cbIsNew = view.findViewById(R.id.cb_is_new)
        cbIsHot = view.findViewById(R.id.cb_is_hot)
        spinnerCategory = view.findViewById(R.id.spinner_category)
        ivImage = view.findViewById(R.id.iv_selected_image)
        btnSelectImage = view.findViewById(R.id.btn_select_image)
        btnUpdate = view.findViewById(R.id.btn_update_product)

        repository = ProductRepository(RetrofitClient.instance)
        categoryRepository = CategoryRepository(RetrofitClient.instance)

        // Load category từ backend
        loadCategories {
            // Khi category load xong, load dữ liệu sản phẩm để set selection spinner
            loadProductData()
        }

        // Chọn ảnh mới
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST)
        }

        btnUpdate.setOnClickListener {
            updateProduct()
        }

        return view
    }

    private fun loadCategories(onLoaded: (() -> Unit)? = null) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { categoryRepository.getAllCategories() }
                if (response.isSuccessful && response.body()?.success == true) {
                    categories = response.body()?.data ?: emptyList()
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categories.map { it.name ?: "N/A" }
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = adapter
                    onLoaded?.invoke()
                } else {
                    Toast.makeText(requireContext(), "Không tải được danh mục", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi load category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                ivImage.visibility = View.VISIBLE
                ivImage.setImageURI(it)
            }
        }
    }

    private fun loadProductData() {
        if (productId == 0L || categories.isEmpty()) return // chờ category load xong

        lifecycleScope.launch {
            try {
                val response = repository.getProductById(productId)
                if (response.isSuccessful) {
                    response.body()?.data?.let { product ->
                        etName.setText(product.name)
                        etSlug.setText(product.slug)
                        etPrice.setText(product.price?.toLong()?.toString() ?: "")
                        etDiscountPrice.setText(product.discountprice?.toLong()?.toString() ?: "")
                        etBrand.setText(product.brand)
                        etStock.setText(product.stockquantity.toString())
                        etDescription.setText(product.description)
                        cbIsNew.isChecked = product.isNew == true
                        cbIsHot.isChecked = product.isHot == true

                        val categoryPosition = categories.indexOfFirst { c ->
                            c.id != null && product.category?.id != null && c.id == product.category!!.id
                        }
                        if (categoryPosition != -1) spinnerCategory.setSelection(categoryPosition)

                        product.imageUrl?.let { url ->
                            val fullUrl = if (url.startsWith("http")) url else "http://10.0.2.2:8080$url"
                            Glide.with(this@UpdateProductFragment)
                                .load(fullUrl)
                                .into(ivImage)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi load product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProduct() {
        val name = etName.text.toString().trim()
        val slug = etSlug.text.toString().trim()
        val price = etPrice.text.toString().trim()
        val discountPrice = etDiscountPrice.text.toString().trim()
        val brand = etBrand.text.toString().trim()
        val stock = etStock.text.toString().trim()
        val isNew = cbIsNew.isChecked
        val isHot = cbIsHot.isChecked
        val description = etDescription.text.toString().trim()
        val category = categories[spinnerCategory.selectedItemPosition]

        if (name.isEmpty() || price.isEmpty()) {
            Toast.makeText(requireContext(), "Tên và giá là bắt buộc", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val slugBody = slug.toRequestBody("text/plain".toMediaTypeOrNull())
                val priceBody = price.toRequestBody("text/plain".toMediaTypeOrNull())
                val discountBody = (if (discountPrice.isEmpty()) "0" else discountPrice).toRequestBody("text/plain".toMediaTypeOrNull())
                val brandBody = brand.toRequestBody("text/plain".toMediaTypeOrNull())
                val stockBody = stock.toRequestBody("text/plain".toMediaTypeOrNull())
                val isNewBody = isNew.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val isHotBody = isHot.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryBody = (category.id ?: 0L).toString().toRequestBody("text/plain".toMediaTypeOrNull())

                var filePart: MultipartBody.Part? = null
                selectedImageUri?.let { uri ->
                    val file = File(requireContext().cacheDir, "temp_image")
                    requireContext().contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    filePart = MultipartBody.Part.createFormData(
                        "files",
                        file.name,
                        file.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                }

                val response = withContext(Dispatchers.IO) {
                    repository.updateProductWithImage(
                        productId, nameBody, slugBody, priceBody, discountBody,
                        brandBody, stockBody, isNewBody, isHotBody, descriptionBody, categoryBody, filePart
                    )
                }

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "Cập nhật sản phẩm thành công!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Cập nhật thất bại: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


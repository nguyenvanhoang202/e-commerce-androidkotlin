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
    private val categories = listOf(
        Category(1L, "Điện thoại", null, null),
        Category(2L, "Laptop", null, null),
        Category(3L, "Phụ kiện", null, null)
    )

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {
        val view = inflater.inflate(R.layout.fragment_update_product, container, false)
        val btnBack: ImageButton = view.findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        val product = arguments?.getSerializable("product") as? Product
        product?.let {
            productId = it.id ?: 0L}
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

        repository = ProductRepository(RetrofitClient.instance) // Khởi tạo repository

        // Setup spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Chọn ảnh mới
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST)
        }

        // Cập nhật sản phẩm
        btnUpdate.setOnClickListener {
            updateProduct()
        }

        // Nếu bạn có productId, có thể load data trước
        loadProductData()

        return view
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
        // Nếu productId > 0, load dữ liệu từ API
        if (productId == 0L) return

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
                        if (categoryPosition != -1) {
                            spinnerCategory.setSelection(categoryPosition)
                        }
                        product.imageUrl?.let { url ->
                            // Nếu đường dẫn trả về không có http (tức là chỉ có /uploads/...), thì nối thêm host vào
                            val fullUrl = if (url.startsWith("http")) {
                                url
                            } else {
                                "http://10.0.2.2:8080$url"
                            }

                            Glide.with(this@UpdateProductFragment)
                                .load(fullUrl)
                                .into(ivImage)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi load data: ${e.message}", Toast.LENGTH_SHORT).show()
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

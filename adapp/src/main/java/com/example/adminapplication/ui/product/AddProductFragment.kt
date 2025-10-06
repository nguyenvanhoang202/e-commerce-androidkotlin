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
import com.example.adminapplication.R
import com.example.adminapplication.data.api.RetrofitClient
import com.example.adminapplication.data.model.Category
import com.example.adminapplication.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddProductFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etSlug: EditText
    private lateinit var etPrice: EditText
    private lateinit var etDiscountPrice: EditText
    private lateinit var etBrand: EditText
    private lateinit var etStock: EditText
    private lateinit var etDescription: EditText
    private lateinit var cbIsNew: CheckBox
    private lateinit var cbIsHot: CheckBox
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnSave: Button
    private lateinit var btnSelectImage: Button
    private lateinit var ivSelectedImage: ImageView

    private var selectedImageUri: Uri? = null
    private lateinit var repository: ProductRepository

    private val categories = listOf(
        Category(1L, "Điện thoại", null, null),
        Category(2L, "Máy tính", null, null),
        Category(3L, "Laptop", null, null)
    )


    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_product, container, false)
        // Nút back
        val btn_Back: ImageButton = view.findViewById(R.id.btn_back)
        btn_Back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
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
        btnSave = view.findViewById(R.id.btn_save_product)
        btnSelectImage = view.findViewById(R.id.btn_select_image)
        ivSelectedImage = view.findViewById(R.id.iv_selected_image)

        repository = ProductRepository(RetrofitClient.instance)

        // Setup Spinner
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories.map { it.name })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Chọn ảnh
        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST)
        }

        // Lưu sản phẩm
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val slug = etSlug.text.toString().trim()
            val price = etPrice.text.toString().toDoubleOrNull()
            val discountPrice = etDiscountPrice.text.toString().toDoubleOrNull() ?: 0.0
            val brand = etBrand.text.toString().trim()
            val stock = etStock.text.toString().toIntOrNull() ?: 0
            val description = etDescription.text.toString().trim()
            val isNew = cbIsNew.isChecked
            val isHot = cbIsHot.isChecked
            val category = categories[spinnerCategory.selectedItemPosition]


            if (name.isEmpty() || price == null) {
                Toast.makeText(requireContext(), "Tên và giá là bắt buộc", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn ảnh sản phẩm", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val imageFile = File(requireContext().cacheDir, "temp_image")
                    requireContext().contentResolver.openInputStream(selectedImageUri!!)?.use { input ->
                        imageFile.outputStream().use { output -> input.copyTo(output) }
                    }

                    val nameBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                    val slugBody = slug.toRequestBody("text/plain".toMediaTypeOrNull())
                    val priceBody = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val discountBody = discountPrice.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val brandBody = brand.toRequestBody("text/plain".toMediaTypeOrNull())
                    val stockBody = stock.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val isNewBody = isNew.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val isHotBody = isHot.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                    val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val categoryIdBody = (category.id ?: 0L).toString().toRequestBody("text/plain".toMediaTypeOrNull())


                    val filePart = MultipartBody.Part.createFormData(
                        "files",
                        imageFile.name,
                        imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                    )

                    val response = withContext(Dispatchers.IO) {
                        repository.createProductWithImage(
                            nameBody, slugBody, priceBody, discountBody, brandBody,
                            stockBody, isNewBody, isHotBody,descriptionBody, categoryIdBody, filePart
                        )
                    }

                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.setFragmentResult("product_added", Bundle().apply { putBoolean("success", true) })
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Thêm thất bại: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }


        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                ivSelectedImage.visibility = View.VISIBLE
                ivSelectedImage.setImageURI(it)
                Toast.makeText(requireContext(), "Đã chọn ảnh", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.example.adminapplication.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.adminapplication.R
import com.example.adminapplication.data.api.RetrofitClient
import com.example.adminapplication.data.repository.CategoryRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AddCategoryBottomSheet(
    private val onAdded: () -> Unit // callback reload list sau khi thêm
) : BottomSheetDialogFragment() {

    private val repository = CategoryRepository(RetrofitClient.instance)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_add_category, container, false)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etSlug = view.findViewById<EditText>(R.id.etSlug)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val slug = etSlug.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Tên danh mục không được để trống"
                etName.requestFocus()
                return@setOnClickListener
            }

            if (slug.isEmpty()) {
                etSlug.error = "Slug không được để trống"
                etSlug.requestFocus()
                return@setOnClickListener
            }

            // Kiểm tra định dạng slug
            if (!slug.matches(Regex("^[a-z0-9-]+$"))) {
                etSlug.error = "Slug chỉ được chứa chữ cái thường, số và dấu gạch ngang"
                etSlug.requestFocus()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = repository.createCategory(name, slug, description)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true) {
                            Toast.makeText(requireContext(), "Thêm danh mục thành công", Toast.LENGTH_SHORT).show()
                            onAdded()
                            dismiss()
                        } else {
                            val message = apiResponse?.message ?: "Lỗi không xác định từ server"
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Không có thông tin lỗi"
                        Toast.makeText(
                            requireContext(),
                            "Lỗi HTTP ${response.code()}: $errorBody",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: HttpException) {
                    Toast.makeText(
                        requireContext(),
                        "Lỗi HTTP: ${e.code()} - ${e.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: IOException) {
                    Toast.makeText(requireContext(), "Lỗi mạng: Vui lòng kiểm tra kết nối", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Lỗi: ${e.message ?: "Không xác định"}", Toast.LENGTH_LONG).show()
                }
            }
        }

        return view
    }

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme
}
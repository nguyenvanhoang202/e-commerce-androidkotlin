package com.example.adminapplication.ui.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.adminapplication.R
import com.example.adminapplication.data.api.RetrofitClient
import com.example.adminapplication.data.model.Product
import com.example.adminapplication.data.model.ProductImage
import com.example.adminapplication.data.repository.ProductImageRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ProductImagesFragment : Fragment() {

    private lateinit var gridView: GridView
    private lateinit var btnAddImage: ImageButton
    private lateinit var bottomActionBar: LinearLayout
    private lateinit var btnDelete: Button
    private lateinit var btnEdit: Button

    private lateinit var product: Product
    private val selectedImages = mutableSetOf<Int>()
    private val images = mutableListOf<ProductImage>()
    private lateinit var adapter: ImageAdapter
    private val repository = ProductImageRepository(RetrofitClient.instance)

    private val baseUrl = "http://10.0.2.2:8080"

    companion object {
        private const val PICK_IMAGE_REQUEST = 1002
        private const val PICK_EDIT_IMAGE_REQUEST = 2024
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        product = arguments?.getSerializable("product") as? Product
            ?: throw IllegalArgumentException("Product phải được truyền vào ProductImagesFragment")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_product_image, container, false)

        gridView = view.findViewById(R.id.grid_images)
        btnAddImage = view.findViewById(R.id.btn_add_image)
        bottomActionBar = view.findViewById(R.id.bottom_action_bar)
        btnDelete = view.findViewById(R.id.btn_delete_selected)
        btnEdit = view.findViewById(R.id.btn_edit_selected)

        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adapter = ImageAdapter()
        gridView.adapter = adapter

        btnAddImage.setOnClickListener { openGalleryForUpload() }
        btnDelete.setOnClickListener { deleteSelectedImages() }
        btnEdit.setOnClickListener { editSelectedImages() }

        fetchImagesFromApi()
        return view
    }

    private fun fetchImagesFromApi() {
        lifecycleScope.launch {
            try {
                val response = repository.getAllProductImages(product.id!!)
                if (response.isSuccessful) {
                    images.clear()
                    images.addAll(response.body() ?: emptyList())
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "Lỗi tải ảnh: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi tải ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGalleryForUpload() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST)
    }

    private fun deleteSelectedImages() {
        val idsToDelete = selectedImages.map { images[it].id }
        if (idsToDelete.isEmpty()) return

        lifecycleScope.launch {
            try {
                val response = repository.deleteImages(idsToDelete)
                val body = response.body()

                if (response.isSuccessful && body != null && body.success) {
                    idsToDelete.sortedDescending().forEach { id ->
                        images.removeAll { it.id == id }
                    }
                    selectedImages.clear()
                    adapter.notifyDataSetChanged()
                    bottomActionBar.visibility = View.GONE
                    Toast.makeText(requireContext(), body.message ?: "Xóa thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), body?.message ?: "Xóa thất bại", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ Hàm sửa ảnh (mới)
    private fun editSelectedImages() {
        if (selectedImages.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn ảnh cần sửa", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh thay thế"), PICK_EDIT_IMAGE_REQUEST)
    }

    // ✅ Nhận kết quả chọn ảnh (cả upload và edit)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null) return

        val imageUris = mutableListOf<Uri>()
        data.clipData?.let {
            for (i in 0 until it.itemCount) {
                imageUris.add(it.getItemAt(i).uri)
            }
        } ?: data.data?.let { imageUris.add(it) }

        when (requestCode) {
            PICK_IMAGE_REQUEST -> if (imageUris.isNotEmpty()) uploadImages(imageUris)
            PICK_EDIT_IMAGE_REQUEST -> if (imageUris.isNotEmpty()) updateSelectedImages(imageUris)
        }
    }

    private fun uploadImages(uris: List<Uri>) {
        val parts = uris.map { uri ->
            val file = File(requireContext().cacheDir, System.currentTimeMillis().toString() + ".jpg")
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
            MultipartBody.Part.createFormData("files", file.name, requestFile)
        }

        lifecycleScope.launch {
            try {
                val response = repository.uploadImages(product.id!!, parts)
                if (response.isSuccessful) {
                    response.body()?.let { images.addAll(it) }
                    adapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Upload ảnh thành công", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Upload thất bại: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ Gọi API update ảnh
    private fun updateSelectedImages(newImageUris: List<Uri>) {
        val imageIds = selectedImages.map { images[it].id }

        lifecycleScope.launch {
            try {
                val parts = newImageUris.map { uri ->
                    val file = File(requireContext().cacheDir, "update_" + System.currentTimeMillis() + ".jpg")
                    requireContext().contentResolver.openInputStream(uri)?.use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                    val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), file)
                    MultipartBody.Part.createFormData("files", file.name, requestBody)
                }

                val response = repository.updateImages(imageIds, parts)

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Cập nhật ảnh thành công", Toast.LENGTH_SHORT).show()
                    fetchImagesFromApi()
                    selectedImages.clear()
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "Cập nhật thất bại: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class ImageAdapter : BaseAdapter() {
        override fun getCount(): Int = images.size
        override fun getItem(position: Int): Any = images[position]
        override fun getItemId(position: Int): Long = images[position].id
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val imageView = ImageView(requireContext())
            val padding = 8
            imageView.layoutParams = AbsListView.LayoutParams(300, 300)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(padding, padding, padding, padding)

            Glide.with(requireContext())
                .load(baseUrl + images[position].imageUrl)
                .into(imageView)

            // ✅ Thêm viền cam khi được chọn
            if (selectedImages.contains(position)) {
                imageView.setBackgroundResource(R.drawable.image_selected_border)
            } else {
                imageView.setBackgroundColor(Color.TRANSPARENT)
            }

            imageView.setOnClickListener {
                if (selectedImages.contains(position)) selectedImages.remove(position)
                else selectedImages.add(position)
                notifyDataSetChanged()
                bottomActionBar.visibility = if (selectedImages.isEmpty()) View.GONE else View.VISIBLE
            }

            return imageView
        }

    }
}

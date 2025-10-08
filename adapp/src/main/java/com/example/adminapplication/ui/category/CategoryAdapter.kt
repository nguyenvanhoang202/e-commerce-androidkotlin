package com.example.adminapplication.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.adminapplication.R
import com.example.adminapplication.data.model.Category

class CategoryAdapter(
    private val categories: MutableList<Category>,
    private val onEditSave: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvSlug: TextView = itemView.findViewById(R.id.tvSlug)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val layoutHeader: LinearLayout = itemView.findViewById(R.id.layoutHeader)
        val layoutDetails: LinearLayout = itemView.findViewById(R.id.layoutDetails)
        val layoutEditMode: LinearLayout = itemView.findViewById(R.id.layoutEditMode)

        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val btnSave: ImageButton = itemView.findViewById(R.id.btnSave)

        val etName: EditText? = itemView.findViewById(R.id.etName)
        val etSlug: EditText? = itemView.findViewById(R.id.etSlug)
        val etDescription: EditText? = itemView.findViewById(R.id.etDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.tvName.text = category.name
        holder.tvSlug.text = "Slug: ${category.slug}"
        holder.tvDescription.text = "Mô tả: ${category.description ?: "(Không có mô tả)"}"

        // Toggle chi tiết
        holder.layoutHeader.setOnClickListener {
            val visible = holder.layoutDetails.visibility == View.VISIBLE
            holder.layoutDetails.visibility = if (visible) View.GONE else View.VISIBLE
            // ẩn chế độ edit khi đóng/open lại
            if (!visible) holder.layoutEditMode.visibility = View.GONE
        }

        holder.btnEdit.setOnClickListener {
            val isEditing = holder.layoutEditMode.visibility == View.VISIBLE

            if (isEditing) {
                // Nếu đang ở chế độ sửa → ẩn form sửa, quay về xem chi tiết
                holder.layoutEditMode.visibility = View.GONE
                holder.layoutDetails.visibility = View.GONE
            } else {
                // Nếu đang ở chế độ xem → mở form sửa
                holder.layoutDetails.visibility = View.VISIBLE
                holder.layoutEditMode.visibility = View.VISIBLE

                holder.etName?.setText(category.name)
                holder.etSlug?.setText(category.slug)
                holder.etDescription?.setText(category.description ?: "")
            }
        }

        holder.btnSave.setOnClickListener {
            // validate ngắn gọn
            val newName = holder.etName?.text.toString().trim()
            val newSlug = holder.etSlug?.text.toString().trim()
            val newDesc = holder.etDescription?.text.toString().trim()

            if (newName.isEmpty()) {
                Toast.makeText(holder.itemView.context, "Tên không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updated = category.copy(
                name = newName,
                slug = newSlug,
                description = newDesc
            )

            onEditSave(updated) // Fragment sẽ gọi API và reload
            holder.layoutEditMode.visibility = View.GONE
        }

        holder.btnDelete.setOnClickListener {
            onDelete(category)
        }
    }

    override fun getItemCount(): Int = categories.size

    // ---- Public helper để fragment cập nhật danh sách ----
    fun setData(newList: List<Category>) {
        categories.clear()
        categories.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateItem(updated: Category) {
        val idx = categories.indexOfFirst { it.id == updated.id }
        if (idx >= 0) {
            categories[idx] = updated
            notifyItemChanged(idx)
        }
    }

    fun removeItemById(id: Long?) {
        val idx = categories.indexOfFirst { it.id == id }
        if (idx >= 0) {
            categories.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }
}

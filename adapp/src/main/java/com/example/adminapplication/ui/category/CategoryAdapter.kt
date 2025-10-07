package com.example.adminapplication.ui.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adminapplication.R
import com.example.adminapplication.data.model.Category

class CategoryAdapter(
    private val categories: MutableList<Category>,
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvCategoryName)
        val tvSlug: TextView = itemView.findViewById(R.id.tvSlug)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
        val layoutDetails: LinearLayout = itemView.findViewById(R.id.layoutDetails)
        val layoutHeader: LinearLayout = itemView.findViewById(R.id.layoutHeader)
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

        // Ẩn/hiện chi tiết khi nhấn vào tên danh mục
        holder.layoutHeader.setOnClickListener {
            val isVisible = holder.layoutDetails.visibility == View.VISIBLE
            holder.layoutDetails.visibility = if (isVisible) View.GONE else View.VISIBLE
        }

        // Sửa danh mục
        holder.btnEdit.setOnClickListener { onEdit(category) }

        // Xóa danh mục
        holder.btnDelete.setOnClickListener { onDelete(category) }
    }

    override fun getItemCount(): Int = categories.size
}

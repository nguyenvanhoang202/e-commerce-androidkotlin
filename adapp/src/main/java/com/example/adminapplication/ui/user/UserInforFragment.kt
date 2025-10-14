package com.example.adminapplication.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.adminapplication.R
import com.example.adminapplication.data.api.RetrofitClient
import com.example.adminapplication.data.model.UserWithDetailDTO
import com.example.adminapplication.data.repository.UserRepository
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserInforFragment : Fragment() {
    private lateinit var rgActive: RadioGroup
    private lateinit var rbActive: RadioButton
    private lateinit var rbInactive: RadioButton

    private lateinit var ivAvatar: ImageView
    private lateinit var btnEdit: ImageButton
    private lateinit var btnSave: ImageButton

    // TextView cơ bản
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvActive: TextView
    private lateinit var tvCreatedAt: TextView

    // TextView chi tiết
    private lateinit var tvFullName: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvBirthday: TextView
    private lateinit var tvGender: TextView

    // EditText nhập liệu
    private lateinit var edtUsername: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtRole: EditText
    private lateinit var edtActive: EditText

    private lateinit var edtFullName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var edtBirthday: EditText
    private lateinit var edtGender: EditText

    private var isEditMode = false
    private var userData: UserWithDetailDTO? = null
    private val userRepo = UserRepository( RetrofitClient.instance)
    private var userId: Long = 0L


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_infor, container, false)
        initViews(view)
        setupListeners()
        // Nút back
        view.findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // lấy id từ arguments (khi truyền từ UserFragment)
        userId = arguments?.getLong("user_id") ?: 0L

        if (userId > 0) {
            loadUserInfo(userId)
        } else {
            Toast.makeText(requireContext(), "Không tìm thấy ID user", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    // --- Setup ---
    private fun initViews(view: View) {
        ivAvatar = view.findViewById(R.id.ivAvatar)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnSave = view.findViewById(R.id.btnSave)
        rgActive = view.findViewById(R.id.rgActive)
        rbActive = view.findViewById(R.id.rbActive)
        rbInactive = view.findViewById(R.id.rbInactive)
        // TextViews
        tvUsername = view.findViewById(R.id.tvUsername)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvRole = view.findViewById(R.id.tvRole)
        tvActive = view.findViewById(R.id.tvActive)
        tvCreatedAt = view.findViewById(R.id.tvCreatedAt)
        tvFullName = view.findViewById(R.id.tvFullName)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvAddress = view.findViewById(R.id.tvAddress)
        tvBirthday = view.findViewById(R.id.tvBirthday)
        tvGender = view.findViewById(R.id.tvGender)

        // EditTexts
        edtUsername = EditText(requireContext())
        edtEmail = EditText(requireContext())
        edtRole = EditText(requireContext())
        edtActive = EditText(requireContext())

        listOf(edtUsername, edtEmail, edtRole, edtActive).forEach {
            it.visibility = View.GONE
        }

        replaceTextViewWithEditText(tvUsername, edtUsername)
        replaceTextViewWithEditText(tvEmail, edtEmail)
        replaceTextViewWithEditText(tvRole, edtRole)
        replaceTextViewWithEditText(tvActive, edtActive)


        edtFullName = EditText(requireContext())
        edtPhone = EditText(requireContext())
        edtAddress = EditText(requireContext())
        edtBirthday = EditText(requireContext())
        edtGender = EditText(requireContext())

        listOf(edtFullName, edtPhone, edtAddress, edtBirthday, edtGender).forEach {
            it.visibility = View.GONE
        }

        // Thêm EditText ngay sau TextView tương ứng
        replaceTextViewWithEditText(tvFullName, edtFullName)
        replaceTextViewWithEditText(tvPhone, edtPhone)
        replaceTextViewWithEditText(tvAddress, edtAddress)
        replaceTextViewWithEditText(tvBirthday, edtBirthday)
        replaceTextViewWithEditText(tvGender, edtGender)
    }

    private fun setupListeners() {
        btnEdit.setOnClickListener { toggleEditMode(true) }
        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    // --- Load dữ liệu từ API ---
    private fun loadUserInfo(id: Long) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    userRepo.getUserDetailById(id)
                }

                if (response.success && response.data != null) {
                    userData = response.data
                    displayUserInfo(userData)
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Hiển thị dữ liệu ---
    private fun displayUserInfo(user: UserWithDetailDTO?) {
        user ?: return
        tvUsername.text = user.username
        tvEmail.text = user.email
        tvRole.text = user.role
        tvActive.text = if (user.active) "Hoạt động" else "Khóa"
        tvCreatedAt.text = user.createdAt

        tvFullName.text = user.fullName
        tvPhone.text = user.phone
        tvAddress.text = user.address
        tvBirthday.text = user.birthday
        tvGender.text = user.gender

        // ✅ Nối base URL nếu backend chỉ trả về đường dẫn tương đối
        val baseUrl = "http://10.0.2.2:8080" // hoặc IP thật của server khi build release
        val fullAvatarUrl = if (user.avatar?.startsWith("http") == true) {
            user.avatar
        } else {
            baseUrl + user.avatar
        }

        Picasso.get()
            .load(fullAvatarUrl)
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.image_placeholder)
            .into(ivAvatar)
    }

    // --- Chuyển đổi giữa View và Edit ---
    private fun toggleEditMode(enable: Boolean) {
        isEditMode = enable
        btnEdit.visibility = if (enable) View.GONE else View.VISIBLE
        btnSave.visibility = if (enable) View.VISIBLE else View.GONE

        val textViews = listOf(
            tvUsername, tvEmail, tvRole, tvActive,
            tvFullName, tvPhone, tvAddress, tvBirthday, tvGender
        )

        val editTexts = listOf(
            edtUsername, edtEmail, edtRole, edtActive,
            edtFullName, edtPhone, edtAddress, edtBirthday, edtGender
        )


        for (i in textViews.indices) {
            textViews[i].visibility = if (enable) View.GONE else View.VISIBLE
            editTexts[i].visibility = if (enable) View.VISIBLE else View.GONE
            if (enable) {
                editTexts[i].setText(textViews[i].text)
            }
        }
        // Xử lý riêng cho trạng thái active
        tvActive.visibility = if (enable) View.GONE else View.VISIBLE
        rgActive.visibility = if (enable) View.VISIBLE else View.GONE

        if (enable) {
            if (userData?.active == true) rbActive.isChecked = true
            else rbInactive.isChecked = true
        }

    }

    // --- Lưu thay đổi ---
    private fun saveChanges() {
        val user = userData ?: return
        val detailBody = mutableMapOf<String, Any>()

        // chỉ thêm field thay đổi
        if (edtUsername.text.toString() != user.username) detailBody["username"] = edtUsername.text.toString()
        if (edtEmail.text.toString() != user.email) detailBody["email"] = edtEmail.text.toString()
        if (edtRole.text.toString() != user.role) detailBody["role"] = edtRole.text.toString()
        // Xử lý riêng cho trạng thái active
        val isActiveChecked = rbActive.isChecked
        if (isActiveChecked != user.active) detailBody["active"] = isActiveChecked
        if (edtActive.text.toString() != (if (user.active) "Hoạt động" else "Khóa"))
            detailBody["active"] = edtActive.text.toString().equals("hoạt động", ignoreCase = true)
        if (edtFullName.text.toString() != user.fullName) detailBody["fullName"] = edtFullName.text.toString()
        if (edtPhone.text.toString() != user.phone) detailBody["phone"] = edtPhone.text.toString()
        if (edtAddress.text.toString() != user.address) detailBody["address"] = edtAddress.text.toString()
        if (edtBirthday.text.toString() != user.birthday) detailBody["birthday"] = edtBirthday.text.toString()
        if (edtGender.text.toString() != user.gender) detailBody["gender"] = edtGender.text.toString()

        if (detailBody.isEmpty()) {
            Toast.makeText(requireContext(), "Không có thay đổi", Toast.LENGTH_SHORT).show()
            toggleEditMode(false)
            return
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    // Gọi API cập nhật user + detail
                    userRepo.updateUserDetail(userId, detailBody)
                }

                // Nếu response.success == true thì cập nhật thành công
                if (response.success) {
                    Toast.makeText(requireContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                    toggleEditMode(false)
                    loadUserInfo(userId) // reload dữ liệu mới
                } else {
                    Toast.makeText(requireContext(), response.message ?: "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Lỗi khi cập nhật: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
            parentFragmentManager.setFragmentResult("user_updated", Bundle())
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Khi quay lại, hiện lại nút Add
        parentFragmentManager.setFragmentResult("toggle_add_button", Bundle().apply {
            putBoolean("show", true)
        })
    }


    private fun replaceTextViewWithEditText(tv: TextView, edt: EditText) {
        val parent = tv.parent as ViewGroup
        val index = parent.indexOfChild(tv)
        parent.addView(edt, index + 1)
    }
    companion object {
        fun newInstance(userId: Long): UserInforFragment {
            val fragment = UserInforFragment()
            val args = Bundle()
            args.putLong("user_id", userId)
            fragment.arguments = args
            return fragment
        }
    }
}

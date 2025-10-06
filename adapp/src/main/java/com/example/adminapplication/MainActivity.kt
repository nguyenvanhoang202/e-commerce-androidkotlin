package com.example.adminapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.fragment.app.Fragment
import com.example.adminapplication.ui.dashboard.DashboardFragment
import com.example.adminapplication.ui.order.OrderFragment
import com.example.adminapplication.ui.product.ProductFragment
import com.example.adminapplication.ui.category.CategoryFragment
import com.example.adminapplication.ui.notification.NotificationFragment
import com.example.adminapplication.ui.user.UserFragment
import com.example.adminapplication.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        tvTitle = findViewById(R.id.tvTitle)

        val btnMenu = findViewById<ImageView>(R.id.btnMenu)
        val btnChat = findViewById<ImageView>(R.id.btnChat)

        //  Mặc định mở Dashboard
        replaceFragment(DashboardFragment())
        tvTitle.text = "Dashboard"

        //  Nút 3 gạch trái mở menu
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        //  Nút chat bên phải
        btnChat.setOnClickListener {
            Toast.makeText(this, "Chức năng chat đang phát triển", Toast.LENGTH_SHORT).show()
        }

        //  Xử lý chọn item trong menu
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawer(GravityCompat.START)

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    replaceFragment(DashboardFragment())
                    tvTitle.text = "Dashboard"
                }
                R.id.nav_order -> {
                    replaceFragment(OrderFragment())
                    tvTitle.text = "Quản lý đơn hàng"
                }
                R.id.nav_product -> {
                    replaceFragment(ProductFragment())
                    tvTitle.text = "Quản lý sản phẩm"
                }
                R.id.nav_category -> {
                    replaceFragment(CategoryFragment())
                    tvTitle.text = "Quản lý danh mục"
                }
                R.id.nav_notifications -> {
                    replaceFragment(NotificationFragment())
                    tvTitle.text = "Quản lý thông báo"
                }
                R.id.nav_user -> {
                    replaceFragment(UserFragment())
                    tvTitle.text = "Quản lý người dùng"
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    tvTitle.text = "Tài khoản"
                }
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}

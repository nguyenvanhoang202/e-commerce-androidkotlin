package com.example.adminapplication.ui.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.adminapplication.R

class OrderFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Nạp layout cho Dashboard
        return inflater.inflate(R.layout.fragment_order, container, false)
    }
}
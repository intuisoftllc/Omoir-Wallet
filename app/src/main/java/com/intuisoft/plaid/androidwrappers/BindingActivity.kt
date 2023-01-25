package com.intuisoft.plaid.androidwrappers

import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BindingActivity<T: ViewBinding> : AppCompatActivity() {

    protected var _binding: T? = null
    protected val binding get() = _binding!!

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    fun withBinding(delegate: T.() -> Unit) {
        _binding?.delegate()
    }
}

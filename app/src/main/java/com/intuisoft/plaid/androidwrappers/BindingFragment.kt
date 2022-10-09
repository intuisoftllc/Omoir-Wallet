package com.intuisoft.plaid.androidwrappers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.intuisoft.plaid.MainActivity

abstract class BindingFragment<T: ViewBinding> : Fragment(), FragmentActionBarDelegate {

    protected var _binding: T? = null
    protected val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).isActionBarShowing = showActionBar()

        if(showActionBar()) {
            (requireActivity() as MainActivity).actionBarTitle =
                getString(actionBarTitle())
        }
    }

    abstract fun navigationId() : Int

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
package com.intuisoft.plaid.androidwrappers

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.listeners.NetworkStateChangeListener

abstract class BindingFragment<T: ViewBinding> : Fragment(), FragmentActionBarDelegate, NetworkStateChangeListener, FragmentBottomBarBarDelegate {

    protected var _binding: T? = null
    protected val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as MainActivity).setActionBarVariant(actionBarVariant())
        (requireActivity() as MainActivity).setActionBarTitle(if(actionBarTitle() == 0) "" else resources.getString(actionBarTitle()))
        (requireActivity() as MainActivity).setActionBarSubTitle(if(actionBarSubtitle() == 0) "" else resources.getString(actionBarSubtitle()))
        (requireActivity() as MainActivity).setActionBarActionLeft(actionBarActionLeft())
        (requireActivity() as MainActivity).setActionBarActionRight(actionBarActionRight())
    }

    fun scanBarcode() {
        (requireActivity() as MainActivity).scanBarcode()
    }

    override fun onStateChanged(hasNetwork: Boolean) {
        if(!hasNetwork) {
            Toast.makeText(requireContext(), getString(R.string.no_connection), Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
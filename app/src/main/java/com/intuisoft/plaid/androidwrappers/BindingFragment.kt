package com.intuisoft.plaid.androidwrappers

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.androidwrappers.delegates.FragmentActionBarDelegate
import com.intuisoft.plaid.androidwrappers.delegates.FragmentBottomBarBarDelegate
import com.intuisoft.plaid.listeners.NetworkStateChangeListener

abstract class BindingFragment<T: ViewBinding> : Fragment(), FragmentActionBarDelegate, NetworkStateChangeListener,
    FragmentBottomBarBarDelegate {

    protected var _binding: T? = null
    protected val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setActionBarVariant(actionBarVariant())
        (activity as? MainActivity)?.setActionBarTitle(if(actionBarTitle() == 0) "" else resources.getString(actionBarTitle()))
        (activity as? MainActivity)?.setActionBarSubTitle(if(actionBarSubtitle() == 0) "" else resources.getString(actionBarSubtitle()))
        (activity as? MainActivity)?.setActionBarActionLeft(actionBarActionLeft())
        (activity as? MainActivity)?.setActionBarActionRight(actionBarActionRight())
        (activity as? MainActivity)?.showBottomBar(showBottomBar())
        activateAnimatedLoading(false, "")
    }

    override fun showBottomBar(): Boolean {
        return false
    }

    override fun onBackPressed() {
        findNavController().popBackStack()
    }

    fun activatePin(setupPin: Boolean, loadingUserData: Boolean) {
        (activity as? MainActivity)?.activatePin(setupPin, loadingUserData)
    }

    fun addToStack(dialog: AppCompatDialog, onCancel: (() -> Unit)? = null) {
        (activity as? MainActivity)?.addToDialogStack(dialog, onCancel)
    }

    fun removeFromStack(dialog: AppCompatDialog) {
        (activity as? MainActivity)?.removeFromDialogStack(dialog)
    }

    fun clearStack() {
        (activity as? MainActivity)?.clearDialogStack()
    }

    fun scanBarcode() {
        requireActivity().checkAppPermission(Manifest.permission.CAMERA, 100) {
            (activity as? MainActivity)?.scanBarcode()
        }
    }

    fun scanInvoice() {
        requireActivity().checkAppPermission(Manifest.permission.CAMERA, 100) {
            (activity as? MainActivity)?.scanInvoice()
        }
    }

    fun activateAnimatedLoading(activate: Boolean, message: String) {
        (activity as? MainActivity)?.activateAnimatedLoading(activate, message)
    }

    fun activateNoInternet(activate: Boolean) {
        (activity as? MainActivity)?.activateNoInternet(activate)
    }

    fun activateContentUnavailable(activate: Boolean, message: String) {
        (activity as? MainActivity)?.activateContentUnavailable(activate, message)
    }

    override fun onNetworkStateChanged(hasNetwork: Boolean) {
        if(!hasNetwork) {
            Toast.makeText(requireContext(), getString(R.string.no_connection), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
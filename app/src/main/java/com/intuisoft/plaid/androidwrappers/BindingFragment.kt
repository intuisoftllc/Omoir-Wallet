package com.intuisoft.plaid.androidwrappers

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.gson.Gson
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.androidwrappers.delegates.FragmentActionBarDelegate
import com.intuisoft.plaid.androidwrappers.delegates.FragmentBottomBarBarDelegate
import com.intuisoft.plaid.common.model.StoredHiddenWalletsModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.listeners.NetworkStateChangeListener
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

    fun softRestart(walletManager: AbstractWalletManager, localStoreRepository: LocalStoreRepository) {
        (this as FragmentBottomBarBarDelegate).apply {
            runBlocking {
                walletManager.stop()
                localStoreRepository.clearCache()

                MainScope().launch {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.putExtra(Constants.Navigation.PASSPHRASES, Gson().toJson(
                        StoredHiddenWalletsModel(walletManager.getHiddenWallets().entries.toList().map { it.key to it.value }), StoredHiddenWalletsModel::class.java))
                    requireActivity().startActivity(intent)
                }
            }
        }
    }

    fun withBinding(delegate: T.() -> Unit) {
        _binding?.delegate()
    }

    fun activatePin(setupPin: Boolean, loadingUserData: Boolean) {
        (activity as? MainActivity)?.activatePin(setupPin, loadingUserData)
    }

    fun addToStack(dialog: Dialog, onCancel: (() -> Unit)? = null) {
        (activity as? MainActivity)?.addToDialogStack(dialog, onCancel)
    }

    fun removeFromStack(dialog: Dialog) {
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
package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.ignoreOnBackPressed
import com.intuisoft.plaid.databinding.FragmentBackupWalletBinding
import com.intuisoft.plaid.databinding.FragmentCreateImportNonCustodialBinding
import com.intuisoft.plaid.databinding.FragmentCreateImportPrivateAndSecureBinding
import com.intuisoft.plaid.databinding.FragmentNameWalletBinding
import com.intuisoft.plaid.databinding.FragmentWalletCreatedBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.Constants
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class WalletCreatedFragment : PinProtectedFragment<FragmentWalletCreatedBinding>() {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWalletCreatedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.check.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800)
        ignoreOnBackPressed()

        binding.home.onClick {
            findNavController().navigate(
                WalletCreatedFragmentDirections.actionGlobalHomescreenFragment(),
                navOptions {
                    popUpTo(R.id.walletCreatedFragment) {
                        inclusive = true
                    }
                }
            )
        }

        binding.gotoDashboard.onClick {

        }
    }

    override fun showActionBar(): Boolean {
        return false
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.nameWalletFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
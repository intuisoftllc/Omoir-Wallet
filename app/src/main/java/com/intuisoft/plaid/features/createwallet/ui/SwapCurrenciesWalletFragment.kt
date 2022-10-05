package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intuisoft.plaid.databinding.FragmentCreateImportNonCustodialBinding
import com.intuisoft.plaid.databinding.FragmentCreateImportPrivateAndSecureBinding
import com.intuisoft.plaid.databinding.FragmentCreateImportSwapCurrenciesBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SwapCurrenciesWalletFragment : PinProtectedFragment<FragmentCreateImportSwapCurrenciesBinding>() {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCreateImportSwapCurrenciesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun showActionBar(): Boolean {
        return false
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
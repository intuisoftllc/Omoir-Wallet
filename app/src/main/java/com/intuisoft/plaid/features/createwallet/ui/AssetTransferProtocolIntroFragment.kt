package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intuisoft.plaid.androidwrappers.ConfigurableFragment
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.databinding.FragmentAssetTransferProtocolIntroBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class AssetTransferProtocolIntroFragment : ConfigurableFragment<FragmentAssetTransferProtocolIntroBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAssetTransferProtocolIntroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        // do nothing
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
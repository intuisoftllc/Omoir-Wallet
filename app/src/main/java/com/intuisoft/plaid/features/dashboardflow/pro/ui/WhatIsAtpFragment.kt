package com.intuisoft.plaid.features.dashboardflow.pro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intuisoft.plaid.androidwrappers.ConfigurableFragment
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.databinding.FragmentWhatIsAtpBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class WhatIsAtpFragment : ConfigurableFragment<FragmentWhatIsAtpBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    protected val viewModel: WalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWhatIsAtpBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())
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
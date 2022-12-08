package com.intuisoft.plaid.features.dashboardflow.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.ConfigurableFragment
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.databinding.FragmentHowDoesAtpWorkBinding
import com.intuisoft.plaid.databinding.FragmentNonCustodialWalletBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class HowDoesAtpWorkFragment : ConfigurableFragment<FragmentHowDoesAtpWorkBinding>(pinProtection = true) {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHowDoesAtpWorkBinding.inflate(inflater, container, false)
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
package com.intuisoft.plaid.features.dashboardflow.pro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.features.createwallet.ZoomOutPageTransformer
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.databinding.FragmentAtpInfoBinding
import com.intuisoft.plaid.features.dashboardflow.pro.adapters.AtpInfoAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel


class AtpInfoFragment : ConfigurableFragment<FragmentAtpInfoBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    protected val viewModel: WalletViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAtpInfoBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        val adapter = AtpInfoAdapter(
            requireActivity()
        )

        binding.close.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
            findNavController().popBackStack()
        }

        binding.useCasesViewpager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.useCasesViewpager)
        binding.useCasesViewpager.setPageTransformer(ZoomOutPageTransformer())
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.atpInfoFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
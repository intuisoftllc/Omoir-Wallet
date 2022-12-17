package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentCreateImportWalletBinding
import com.intuisoft.plaid.features.createwallet.ZoomOutPageTransformer
import com.intuisoft.plaid.features.createwallet.adapters.WalletBenefitsAdapter
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.databinding.FragmentAtpInfoBinding
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.AtpInfoAdapter
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic
import org.koin.androidx.viewmodel.ext.android.viewModel


class AtpInfoFragment : ConfigurableFragment<FragmentAtpInfoBinding>(pinProtection = true) {
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

        binding.close.setOnClickListener {
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
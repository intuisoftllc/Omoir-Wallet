package com.intuisoft.plaid.features.dashboardflow.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.FragmentAtpBinding
import com.intuisoft.plaid.features.dashboardflow.viewmodel.SwapDetailsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AtpFragment : ConfigurableFragment<FragmentAtpBinding>(pinProtection = true) {
    protected val viewModel: SwapDetailsViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAtpBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        onBackPressedCallback {
            onNavigateBottomBarSecondaryFragmentBackwards()
        }

    }

    override fun onNavigateTo(destination: Int) {
        navigate(destination, viewModel.getWalletId())
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.atpFragment
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_alert_red
    }

    override fun onActionLeft() {
        // todo: impl
    }

    override fun actionBarActionRight(): Int {
        return R.drawable.ic_clock
    }

    override fun onActionRight() {
        // todo: impl
    }

    override fun actionBarSubtitle(): Int {
        return R.string.atp
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
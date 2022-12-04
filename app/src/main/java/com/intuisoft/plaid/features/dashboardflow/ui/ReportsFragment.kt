package com.intuisoft.plaid.features.dashboardflow.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.databinding.FragmentExchangeDetailsBinding
import com.intuisoft.plaid.databinding.FragmentReportsBinding
import com.intuisoft.plaid.features.dashboardflow.viewmodel.SwapDetailsViewModel
import com.intuisoft.plaid.model.ExchangeStatus
import com.intuisoft.plaid.util.fragmentconfig.ConfigInvoiceData
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.intuisoft.plaid.util.fragmentconfig.ConfigSwapData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportsFragment : PinProtectedFragment<FragmentReportsBinding>() {
    protected val viewModel: SwapDetailsViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        onBackPressedCallback {
            onNavigateBottomBarSecondaryFragmentBackwards(localStoreRepository)
        }

        binding.inflow.onClick {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    actionBarTitle = 0,
                    actionBarSubtitle = 0,
                    actionBarVariant = 0,
                    actionLeft = 0,
                    actionRight = 0,
                    configurationType = FragmentConfigurationType.CONFIGURATION_INFLOW_REPORT,
                    configData = null
                ),
                Constants.Navigation.WALLET_UUID_BUNDLE_ID to viewModel.getWalletId()
            )

            navigate(
                R.id.reportDetailsFragment,
                bundle
            )
        }

        binding.inflow.onClick {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    actionBarTitle = 0,
                    actionBarSubtitle = 0,
                    actionBarVariant = 0,
                    actionLeft = 0,
                    actionRight = 0,
                    configurationType = FragmentConfigurationType.CONFIGURATION_INFLOW_REPORT,
                    configData = null
                ),
                Constants.Navigation.WALLET_UUID_BUNDLE_ID to viewModel.getWalletId()
            )

            navigate(
                R.id.reportDetailsFragment,
                bundle
            )
        }

        binding.outflow.onClick {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    actionBarTitle = 0,
                    actionBarSubtitle = 0,
                    actionBarVariant = 0,
                    actionLeft = 0,
                    actionRight = 0,
                    configurationType = FragmentConfigurationType.CONFIGURATION_OUTFLOW_REPORT,
                    configData = null
                ),
                Constants.Navigation.WALLET_UUID_BUNDLE_ID to viewModel.getWalletId()
            )

            navigate(
                R.id.reportDetailsFragment,
                bundle
            )
        }

        binding.fees.onClick {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    actionBarTitle = 0,
                    actionBarSubtitle = 0,
                    actionBarVariant = 0,
                    actionLeft = 0,
                    actionRight = 0,
                    configurationType = FragmentConfigurationType.CONFIGURATION_FEE_REPORT,
                    configData = null
                ),
                Constants.Navigation.WALLET_UUID_BUNDLE_ID to viewModel.getWalletId()
            )

            navigate(
                R.id.reportDetailsFragment,
                bundle
            )
        }
    }

    override fun onNavigateTo(destination: Int) {
        navigate(destination, viewModel.getWalletId())
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.reportsFragment
    }

    override fun actionBarSubtitle(): Int {
        return R.string.reports
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
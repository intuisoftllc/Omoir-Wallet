package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.databinding.FragmentReportsBinding
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.SwapDetailsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportsFragment : ConfigurableFragment<FragmentReportsBinding>(pinProtection = true) {
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
                )
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
                )
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
                )
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
                )
            )

            navigate(
                R.id.reportDetailsFragment,
                bundle
            )
        }

        binding.utxDistro.onClick {
            navigate(
                R.id.utxoDistroFragment
            )
        }
    }

    override fun onNavigateTo(destination: Int) {
        navigate(destination)
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
package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.databinding.FragmentWithdrawalTypeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class WithdrawalTypeFragment : ConfigurableFragment<FragmentWithdrawalTypeBinding>(pinProtection = true) {
    protected val viewModel: WalletViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWithdrawalTypeBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.standardWithdrawal.onClick {
            navigate(
                R.id.withdrawalFragment,
                viewModel.getWalletId(),
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }
        binding.invoice.onClick {
            navigate(
                R.id.invoiceFragment,
                viewModel.getWalletId(),
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.withdrawalTypeFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
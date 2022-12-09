package com.intuisoft.plaid.features.dashboardflow.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.local.db.AssetTransferDao
import com.intuisoft.plaid.common.model.AssetTransferModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.FragmentAtpHistoryBinding
import com.intuisoft.plaid.features.dashboardflow.adapters.AtpHistoryAdapter
import com.intuisoft.plaid.features.dashboardflow.viewmodel.AtpHistoryViewModel
import com.intuisoft.plaid.util.fragmentconfig.BasicConfigData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class AtpHistoryFragment : ConfigurableFragment<FragmentAtpHistoryBinding>(pinProtection = true) {
    protected val viewModel: AtpHistoryViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()

    val adapter = AtpHistoryAdapter(
        onItemSelected = ::onTransferSelected,
        getWalletName = ::getWalletName,
        localStoreRepository = localStoreRepository
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAtpHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {

        binding.transfers.adapter = adapter
        viewModel.getTransfers()

        viewModel.transfers.observe(viewLifecycleOwner, Observer {
            adapter.addOrUpdate(it.toArrayList())
        })

        walletManager.databaseUpdated.observe(viewLifecycleOwner, Observer {
            when(it) {
                is AssetTransferDao -> {
                    viewModel.getTransfers()
                }
            }
        })
    }

    private fun onTransferSelected(transfer: AssetTransferModel) {
        var bundle = bundleOf(
            Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                configurationType = FragmentConfigurationType.CONFIGURATION_ATP,
                configData = BasicConfigData(
                    payload = transfer.id
                )
            ),
            Constants.Navigation.WALLET_UUID_BUNDLE_ID to viewModel.getWalletId()
        )

        navigate(
            R.id.atpDetailsFragment,
            bundle,
            Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
        )
    }

    private fun getWalletName(id: String): String {
        return viewModel.getWalletName(id)
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun actionBarSubtitle(): Int {
        return R.string.atp_history_fragment_label
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.atpHistoryFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
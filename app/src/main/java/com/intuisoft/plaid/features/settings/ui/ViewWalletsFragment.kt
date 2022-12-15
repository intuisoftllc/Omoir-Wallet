package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentManageWalletsBinding
import com.intuisoft.plaid.features.homescreen.free.adapters.BasicWalletDataAdapter
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject


class ViewWalletsFragment : ConfigurableFragment<FragmentManageWalletsBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()

    private val adapter = BasicWalletDataAdapter(
        onWalletSelected = ::onWalletSelected,
        localStoreRepository = localStoreRepository
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentManageWalletsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {

        binding.walletsList.adapter = adapter
        walletManager.wallets.observe(viewLifecycleOwner, Observer {
            adapter.addWallets(it.toArrayList())

            binding.walletsList.isVisible = it.isNotEmpty()
            binding.noWalletsContainer.isVisible = it.isEmpty()
        })
    }


    fun onWalletSelected(wallet: LocalWalletModel) {
        var bundle = bundleOf(
            Constants.Navigation.FROM_SETTINGS to true
        )

        navigate(
            R.id.walletSettingsFragment,
            bundle
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN
    }

    override fun actionBarTitle(): Int {
        return R.string.manage_wallets_fragment_label
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun actionBarActionRight(): Int {
        return R.drawable.ic_add
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun onActionRight() {
        navigate(R.id.createWalletFragment)
        // todo: limit to 5 for free version
    }

    override fun navigationId(): Int {
        return R.id.appearanceFragment
    }
}
package com.intuisoft.plaid.features.homescreen.pro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.features.homescreen.shared.viewmodel.HomeScreenViewModel
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.FragmentProHomescreenBinding
import com.intuisoft.plaid.features.homescreen.pro.adapters.ProWalletDataAdapter
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProHomescreenFragment : ConfigurableFragment<FragmentProHomescreenBinding>(
    pinProtection = true,
    requiresWallet = false
), StateListener {
    protected val viewModel: HomeScreenViewModel by viewModel()
    protected val walletVM: WalletViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()

    private val adapter = ProWalletDataAdapter(
        onWalletSelected = ::onWalletSelected,
        localStoreRepository = localStoreRepository
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentProHomescreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {

        onBackPressedCallback {
            requireActivity().finish()
        }

        (requireActivity() as MainActivity).performSetup()
        viewModel.updateGreeting()
        walletVM.refreshLocalCache()

        walletManager.onSyncing.observe(viewLifecycleOwner, Observer {
            binding.swipeContainer.isRefreshing = it
        })

        binding.swipeContainer.setOnRefreshListener {
            if(binding.swipeContainer.isRefreshing) {
                walletManager.synchronizeAll(false)
            }
        }

        showTotalBalance(walletManager.balanceUpdated.value ?: 0)
        walletManager.balanceUpdated.observe(viewLifecycleOwner, Observer {
            showTotalBalance(it)
        })

        binding.totalBalance.setOnClickListener {
            when (walletVM.getDisplayUnit()) {
                BitcoinDisplayUnit.BTC -> {
                    walletVM.setDisplayUnit(BitcoinDisplayUnit.SATS)
                }

                BitcoinDisplayUnit.SATS -> {
                    walletVM.setDisplayUnit(BitcoinDisplayUnit.FIAT)
                }

                BitcoinDisplayUnit.FIAT -> {
                    walletVM.setDisplayUnit(BitcoinDisplayUnit.BTC)
                }
            }

            adapter.updateConversion()
            showTotalBalance(walletManager.balanceUpdated.value ?: 0)
        }

        binding.walletsList.adapter = adapter
        binding.walletsList.layoutManager = GridLayoutManager(requireContext(), 2)
        viewModel.homeScreenGreeting.observe(viewLifecycleOwner, Observer {
            (requireActivity() as MainActivity).setActionBarTitle(it.second)
            (requireActivity() as MainActivity).setActionBarSubTitle(it.first + ",")
        })

        walletManager.wallets.observe(viewLifecycleOwner, Observer {
            adapter.addWallets(it.toArrayList())

            binding.walletsList.isVisible = it.isNotEmpty()
            binding.noWalletsContainer.isVisible = it.isEmpty()
        })

        binding.createWallet.setOnClickListener {
            // todo: limit to 10 for pro version
            navigate(R.id.createWalletFragment)
        }

        walletManager.synchronizeAll(false)
        walletVM.addWalletStateListener(this)
    }

    fun showTotalBalance(totalBalance: Long) {
        binding.totalBalance.text = SimpleCoinNumberFormat.format(localStoreRepository, totalBalance, false)
    }

    override fun onWalletStateUpdated(wallet: LocalWalletModel) {
        adapter.onWalletStateUpdated(wallet)
    }

    override fun onWalletAlreadySynced(wallet: LocalWalletModel) {
        // ignore
    }

    override fun onResume() {
        super.onResume()
        walletManager.closeWallet()
    }

    fun onWalletSelected(wallet: LocalWalletModel) {
        walletManager.openWallet(wallet)
        navigate(R.id.walletProDashboardFragment)
    }

    override fun actionBarVariant(): Int {
        return TopBarView.LEFT_ALIGN
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.proHomescreenFragment
    }

    override fun actionBarActionRight(): Int {
        return R.drawable.ic_settings
    }

    override fun onActionRight() {
        navigate(R.id.settingsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        walletVM.removeWalletSyncListener(this)
    }

}
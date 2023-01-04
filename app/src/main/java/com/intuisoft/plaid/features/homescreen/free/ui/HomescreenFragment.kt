package com.intuisoft.plaid.features.homescreen.free.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventHomescreenCreateWallet
import com.intuisoft.plaid.common.analytics.events.EventHomescreenOpenSettings
import com.intuisoft.plaid.common.analytics.events.EventHomescreenView
import com.intuisoft.plaid.databinding.FragmentHomescreenBinding
import com.intuisoft.plaid.features.homescreen.free.adapters.BasicWalletDataAdapter
import com.intuisoft.plaid.features.homescreen.shared.viewmodel.HomeScreenViewModel
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomescreenFragment : ConfigurableFragment<FragmentHomescreenBinding>(
    pinProtection = true,
    requiresWallet = false
), StateListener {
    protected val viewModel: HomeScreenViewModel by viewModel()
    protected val walletVM: WalletViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()
    protected val eventTracker: EventTracker by inject()

    private val adapter = BasicWalletDataAdapter(
        onWalletSelected = ::onWalletSelected,
        localStoreRepository = localStoreRepository
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomescreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        eventTracker.log(EventHomescreenView())
        (activity as? MainActivity)?.performSetup()
        viewModel.updateGreeting()
        walletVM.addWalletStateListener(this)
        walletVM.refreshLocalCache()

        walletManager.onSyncing.observe(viewLifecycleOwner, Observer {
            binding.swipeContainer.isRefreshing = it
        })

        binding.swipeContainer.setOnRefreshListener {
            if(binding.swipeContainer.isRefreshing) {
                walletManager.synchronizeAll(true)
            }
        }

        binding.walletsView.walletsList.adapter = adapter
        viewModel.homeScreenGreeting.observe(viewLifecycleOwner, Observer {
            (activity as? MainActivity)?.setActionBarTitle(it.second)
            (activity as? MainActivity)?.setActionBarSubTitle(it.first + ",")
        })

        walletManager.wallets.observe(viewLifecycleOwner, Observer {
            adapter.addWallets(it.toArrayList())

            binding.walletsView.walletsList.isVisible = it.isNotEmpty()
            binding.walletsView.noWalletsContainer.isVisible = it.isEmpty()
        })

        walletManager.synchronizeAll(false)
        binding.createWallet.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_MED) {
            if(walletManager.getWalletCount() < Constants.Limit.FREE_MAX_WALLETS) {
                eventTracker.log(EventHomescreenCreateWallet())
                navigate(R.id.createWalletFragment)
            } else {
                styledSnackBar(requireView(), getString(R.string.homescreen_wallet_limit_reached, Constants.Limit.FREE_MAX_WALLETS.toString()))
            }
        }
    }

    override fun onBackPressed() {
        requireActivity().finish()
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
        navigate(R.id.walletDashboardFragment)
    }

    override fun actionBarVariant(): Int {
        return TopBarView.LEFT_ALIGN
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.homescreenFragment
    }

    override fun actionBarActionRight(): Int {
        return R.drawable.ic_settings
    }

    override fun onActionRight() {
        eventTracker.log(EventHomescreenOpenSettings())
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
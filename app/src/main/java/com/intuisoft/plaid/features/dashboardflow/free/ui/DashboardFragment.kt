package com.intuisoft.plaid.features.dashboardflow.free.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.*
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.databinding.FragmentWalletDashboardBinding
import com.intuisoft.plaid.features.homescreen.adapters.BasicTransactionAdapter
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.extensions.safeWalletScope
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.BasicLineChartAdapter
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.DashboardViewModel
import com.intuisoft.plaid.util.SimpleTimeFormat
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.intuisoft.plaid.util.fragmentconfig.BasicConfigData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class DashboardFragment : ConfigurableFragment<FragmentWalletDashboardBinding>(pinProtection = true), StateListener {
    protected val viewModel: DashboardViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()
    protected val eventTracker: EventTracker by inject()

    private val adapter = BasicTransactionAdapter(
        onTransactionSelected = ::onTransactionSelected,
        getConfirmationsForTransaction = ::getConfirmationsForTransaction,
        localStoreRepository = localStoreRepository
    )

    val balanceHistoryAdapter = BasicLineChartAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWalletDashboardBinding.inflate(inflater, container, false)

        setupConfiguration(viewModel)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        onBackPressedCallback {
            onNavigateBottomBarPrimaryFragmentBackwards(localStoreRepository)
        }

        eventTracker.log(EventDashboardView())
        viewModel.getTransactions()
        viewModel.displayCurrentWallet()
        viewModel.showWalletBalance(requireContext())
        viewModel.checkReadOnlyStatus()
        viewModel.addWalletStateListener(this)
        viewModel.refreshLocalCache()

        binding.swipeContainer.setOnRefreshListener {
            if(binding.swipeContainer.isRefreshing) {
                viewModel.syncWallet()
            }
        }

        viewModel.readOnlyWallet.observe(viewLifecycleOwner, Observer {
            binding.withdraw.enableButton(false)
        })

        viewModel.chartData.observe(viewLifecycleOwner, Observer {
            balanceHistoryAdapter.setItems(it)
        })

        viewModel.displayWallet.observe(viewLifecycleOwner, Observer { wallet ->
            (activity as? MainActivity)?.setActionBarTitle(wallet.name)
            onWalletStateUpdated(wallet)
        })

        viewModel.walletBalance.observe(viewLifecycleOwner, Observer {
            (activity as? MainActivity)?.setActionBarSubTitle(it)
        })

        binding.transactions.adapter = adapter
        viewModel.transactions.observe(viewLifecycleOwner, Observer {
            binding.noTransactionsIcon.isVisible = it.isEmpty()
            binding.noTransactionsMessage.isVisible = it.isEmpty()
            binding.transactions.isVisible = it.isNotEmpty()
            viewModel.onTransactionsUpdated(it)


            adapter.addTransactions(it.toArrayList())
        })

        binding.deposit.onClick {
            eventTracker.log(EventDashboardDeposit())
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    actionBarTitle = 0,
                    actionBarSubtitle = 0,
                    actionBarVariant = 0,
                    actionLeft = 0,
                    actionRight = 0,
                    configurationType = FragmentConfigurationType.CONFIGURATION_DISPLAY_SHAREABLE_QR,
                    configData = ConfigQrDisplayData(
                        payload = viewModel.getRecieveAddress(),
                        qrTitle = getString(R.string.export_wallet_receive_btc_title),
                        showClose = true
                    )
                )
            )

            navigate(
                R.id.exportWalletFragment,
                bundle
            )
        }

        binding.withdraw.onClick {
            eventTracker.log(EventDashboardWithdrawal())
            navigate(
                R.id.withdrawalTypeFragment,
                Constants.Navigation.ANIMATED_SLIDE_UP_OPTION
            )
        }
    }

    fun getConfirmationsForTransaction(transaction: TransactionInfo) : Int {
        return viewModel.getConfirmations(transaction)
    }

    fun onTransactionSelected(transaction: TransactionInfo) {
        eventTracker.log(EventDashboardViewTransaction())
        var bundle = bundleOf(
            Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                configurationType = FragmentConfigurationType.CONFIGURATION_TRANSACTION_DATA,
                configData = BasicConfigData(
                    payload = CommonService.getGsonInstance().toJson(transaction)
                )
            )
        )

        navigate(
            R.id.transactionDetailsFragment,
            bundle,
            Constants.Navigation.ANIMATED_FADE_IN_NAV_OPTION
        )
    }

    override fun onWalletStateUpdated(wallet: LocalWalletModel) {
        MainScope().launch {
            safeWalletScope {
                if (wallet.uuid == viewModel.getWalletId() && activity != null && _binding != null) {
                    val state = wallet.onWalletStateChanged(
                        requireContext(),
                        wallet.syncPercentage,
                        false,
                        localStoreRepository
                    )

                    (activity as? MainActivity)?.setActionBarSubTitle(state)

                    if (wallet.isSynced && wallet.isSynced)
                        viewModel.getTransactions()
                    binding.swipeContainer.isRefreshing = wallet.isSyncing
                }
            }
        }
    }

    override fun onWalletAlreadySynced(wallet: LocalWalletModel) {
        MainScope().launch {
            safeWalletScope {
                if (wallet.uuid == viewModel.getWalletId() && activity != null && _binding != null) {
                    binding.swipeContainer.isRefreshing = false
                }
            }
        }
    }

    override fun onNavigateTo(destination: Int) {
        navigate(destination)
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.walletDashboardFragment
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun actionBarActionRight(): Int {
        return R.drawable.ic_settings
    }

    override fun onActionLeft() {
        onNavigateBottomBarPrimaryFragmentBackwards(localStoreRepository)
    }

    override fun onActionRight() {
        eventTracker.log(EventDashboardOpenSettingsOpen())
        navigate(
            R.id.walletSettingsFragment
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeWalletSyncListener(this)
        _binding = null
    }

}
package com.intuisoft.plaid.features.dashboardflow.ui

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
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.databinding.FragmentWalletDashboardBinding
import com.intuisoft.plaid.features.homescreen.adapters.BasicTransactionAdapter
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.intuisoft.plaid.util.fragmentconfig.ConfigTransactionData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : PinProtectedFragment<FragmentWalletDashboardBinding>(), StateListener {
    protected val viewModel: WalletViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()

    private val adapter = BasicTransactionAdapter(
        onTransactionSelected = ::onTransactionSelected,
        getConfirmationsForTransaction = ::getConfirmationsForTransaction,
        localStoreRepository = localStoreRepository
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWalletDashboardBinding.inflate(inflater, container, false)

        setupConfiguration(viewModel)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        viewModel.getTransactions()
        viewModel.displayCurrentWallet()
        viewModel.showWalletBalance(requireContext())
        viewModel.checkReadOnlyStatus()
        viewModel.addWalletStateListener(this)
        viewModel.refreshLocalCache()

        onBackPressedCallback {
            onNavigateBottomBarPrimaryFragmentBackwards(localStoreRepository)
        }

        binding.swipeContainer.setOnRefreshListener {
            if(binding.swipeContainer.isRefreshing) {
                viewModel.syncWallet()
            }
        }

        viewModel.readOnlyWallet.observe(viewLifecycleOwner, Observer {
            binding.withdraw.enableButton(false)
        })

        viewModel.displayWallet.observe(viewLifecycleOwner, Observer { wallet ->
            (requireActivity() as MainActivity).setActionBarTitle(wallet.name)
            onWalletStateUpdated(wallet)
        })

        viewModel.walletBalance.observe(viewLifecycleOwner, Observer {
            (requireActivity() as MainActivity).setActionBarSubTitle(it)
        })

        binding.transactions.adapter = adapter
        viewModel.transactions.observe(viewLifecycleOwner, Observer {
            binding.noTransactionsIcon.isVisible = it.isEmpty()
            binding.noTransactionsMessage.isVisible = it.isEmpty()
            binding.transactions.isVisible = it.isNotEmpty()


            adapter.addTransactions(it.toArrayList())
        })

        binding.deposit.onClick {
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
                        qrTitle = "Receive BTC",
                        showClose = true
                    )
                ),
                Constants.Navigation.WALLET_UUID_BUNDLE_ID to viewModel.getWalletId()
            )

            navigate(
                R.id.exportWalletFragment,
                bundle
            )
        }

        binding.withdraw.onClick {
            navigate(
                R.id.withdrawalTypeFragment,
                viewModel.getWalletId(),
                Constants.Navigation.ANIMATED_SLIDE_UP_OPTION
            )
        }
    }

    fun getConfirmationsForTransaction(transaction: TransactionInfo) : Int {
        return viewModel.getConfirmations(transaction)
    }

    fun onTransactionSelected(transaction: TransactionInfo) {
        var bundle = bundleOf(
            Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                configurationType = FragmentConfigurationType.CONFIGURATION_TRANSACTION_DATA,
                configData = ConfigTransactionData(
                    payload = CommonService.getGsonInstance().toJson(transaction)
                )
            ),
            Constants.Navigation.WALLET_UUID_BUNDLE_ID to viewModel.getWalletId()
        )

        navigate(
            R.id.transactionDetailsFragment,
            bundle,
            Constants.Navigation.ANIMATED_FADE_IN_NAV_OPTION
        )
    }

    override fun onWalletStateUpdated(wallet: LocalWalletModel) {
        if(wallet.uuid == viewModel.getWalletId() && activity != null && _binding != null) {
            (requireActivity() as MainActivity).setActionBarSubTitle(
                wallet.onWalletStateChanged(
                    requireContext(),
                    wallet.syncPercentage,
                    false,
                    localStoreRepository
                )
            )

            if (wallet.isSynced && wallet.isSynced)
                viewModel.getTransactions()
            binding.swipeContainer.isRefreshing = wallet.isSyncing
        }
    }

    override fun onWalletAlreadySynced(wallet: LocalWalletModel) {
        if(wallet.uuid == viewModel.getWalletId() && activity != null && _binding != null) {
            binding.swipeContainer.isRefreshing = false
        }
    }

    override fun onNavigateTo(destination: Int) {
        navigate(destination, viewModel.getWalletId())
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
        navigate(
            R.id.walletSettingsFragment,
            viewModel.getWalletId()
        )
    }

    override fun onSubtitleClicked() {
        if(!viewModel.isWalletSyncing()) {
            when (viewModel.getDisplayUnit()) {
                BitcoinDisplayUnit.BTC -> {
                    viewModel.setDisplayUnit(BitcoinDisplayUnit.SATS)
                }

                BitcoinDisplayUnit.SATS -> {
                    viewModel.setDisplayUnit(BitcoinDisplayUnit.FIAT)
                }

                BitcoinDisplayUnit.FIAT -> {
                    viewModel.setDisplayUnit(BitcoinDisplayUnit.BTC)
                }
            }

            adapter.updateConversion()
            viewModel.showWalletBalance(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeWalletSyncListener(this)
        _binding = null
    }

}
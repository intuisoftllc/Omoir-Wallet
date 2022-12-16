package com.intuisoft.plaid.features.dashboardflow.pro.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.databinding.FragmentProWalletDashboardBinding
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.BasicLineChartAdapter
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.DashboardViewModel
import com.intuisoft.plaid.util.SimpleTimeFormat
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.intuisoft.plaid.util.fragmentconfig.BasicConfigData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class ProDashboardFragment : ConfigurableFragment<FragmentProWalletDashboardBinding>(pinProtection = true), StateListener {
    protected val viewModel: DashboardViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()

    val balanceHistoryAdapter = BasicLineChartAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentProWalletDashboardBinding.inflate(inflater, container, false)

        setupConfiguration(viewModel)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        onBackPressedCallback {
            onNavigateBottomBarPrimaryFragmentBackwards(localStoreRepository)
        }

        viewModel.getTransactions()
        viewModel.displayCurrentWallet()
        viewModel.showWalletBalance(requireContext())
        viewModel.checkReadOnlyStatus()
        viewModel.addWalletStateListener(this)
        viewModel.refreshLocalCache()

        binding.sparkview.setAdapter(balanceHistoryAdapter)
        binding.sparkview.isScrubEnabled = true

        binding.chartContainer.isVisible = localStoreRepository.isProEnabled()
        binding.sparkview.setScrubListener {

            if(it != null) {
                val data = it as ChartDataModel
                binding.percentageGain.visibility = View.INVISIBLE
                binding.scrubTime.visibility = View.VISIBLE
                binding.price.text = viewModel.transformScrubValue(data)

                binding.scrubTime.text = SimpleTimeFormat.getDateByLocale(data.time * Constants.Time.MILLS_PER_SEC, Locale.US)
            } else {
                binding.percentageGain.visibility = View.VISIBLE
                binding.scrubTime.visibility = View.INVISIBLE
                viewModel.showWalletBalance(requireContext())
            }
        }

        binding.price.setOnClickListener {
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

                viewModel.showWalletBalance(requireContext())
                viewModel.onDisplayUnitChanged()
            }
        }

        viewModel.percentageGain.observe(viewLifecycleOwner, Observer { (percentageGain, rawGain) ->
            val rateConverter = RateConverter(
                localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0
            )


            when (localStoreRepository.getBitcoinDisplayUnit()) {
                BitcoinDisplayUnit.SATS,
                BitcoinDisplayUnit.BTC -> {
                    rateConverter.setLocalRate(RateConverter.RateType.SATOSHI_RATE, rawGain)
                }

                BitcoinDisplayUnit.FIAT -> {
                    rateConverter.setLocalRate(RateConverter.RateType.FIAT_RATE, rawGain)
                }
            }

            binding.percentageGain.text = SimpleCoinNumberFormat.formatCurrency(percentageGain) +
                    "% (${rateConverter.from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency()).second})"

            if(percentageGain > 0.0) {
                binding.percentageGain.setTextColor(resources.getColor(R.color.success_color))
            }else if(percentageGain == 0.0) {
                binding.percentageGain.setTextColor(resources.getColor(R.color.text_grey))
            } else {
                binding.percentageGain.setTextColor(resources.getColor(R.color.alt_error_color))
            }
        })

        viewModel.totalSent.observe(viewLifecycleOwner, Observer {
            binding.totalSent.text = it
        })

        viewModel.totalReceived.observe(viewLifecycleOwner, Observer {
            binding.totalReceive.text = it
        })

        viewModel.averagePrice.observe(viewLifecycleOwner, Observer {
            binding.averagePrice.text = it
        })

        viewModel.highestBalance.observe(viewLifecycleOwner, Observer {
            binding.highestBalance.text = it
        })

        viewModel.walletAge.observe(viewLifecycleOwner, Observer {
            binding.age.text = it
        })

        binding.swipeContainer.setOnRefreshListener {
            if(binding.swipeContainer.isRefreshing) {
                viewModel.syncWallet()
            }
        }

        binding.interval1day.setOnClickListener {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_1DAY)
            binding.interval1day.selectTimePeriod(true)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.interval1week.setOnClickListener {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_1WEEK)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(true)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.interval1Month.setOnClickListener {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_1MONTH)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(true)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.interval3Month.setOnClickListener {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_3MONTHS)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(true)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.interval6Month.setOnClickListener {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_6MONTHS)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(true)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.interval1Year.setOnClickListener {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_1YEAR)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(true)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.intervalMax.setOnClickListener {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_ALL_TIME)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(true)
        }

        binding.viewTransactions.onClick {
            navigate(
                R.id.proWalletTransactionsFragment
            )
        }

        viewModel.transactions.observe(viewLifecycleOwner, Observer {
            viewModel.onTransactionsUpdated(it)
        })

        viewModel.readOnlyWallet.observe(viewLifecycleOwner, Observer {
            binding.withdraw.enableButton(false)
        })

        viewModel.chartDataLoading.observe(viewLifecycleOwner, Observer {
            binding.chartDataLoading.isVisible = it
        })

        viewModel.showChartError.observe(viewLifecycleOwner, Observer {
            binding.sparkview.isVisible = it == null
            binding.errorMessage.isVisible = it != null
            if(it != null) binding.chartDataLoading.isVisible = false
            binding.errorMessage.text = it
        })

        viewModel.noChartData.observe(viewLifecycleOwner, Observer {
            binding.sparkview.isVisible = false
            binding.chartDataLoading.isVisible = false
            binding.errorMessage.isVisible = true
            binding.errorMessage.text = getString(R.string.no_data)
        })

        binding.averagePriceContainer.setOnClickListener {
            showBasicInfoBottomSheet(
                context = requireContext(),
                title = getString(R.string.pro_homescreen_average_price_dialog_title),
                message = getString(R.string.pro_homescreen_average_price_dialog_message),
                ::addToStack,
                ::removeFromStack
            )
        }

        viewModel.chartData.observe(viewLifecycleOwner, Observer {
            balanceHistoryAdapter.setItems(it)
        })

        viewModel.displayWallet.observe(viewLifecycleOwner, Observer { wallet ->
            (activity as? MainActivity)?.setActionBarTitle(wallet.name)
            onWalletStateUpdated(wallet)
        })

        viewModel.walletBalance.observe(viewLifecycleOwner, Observer {
            if(localStoreRepository.isProEnabled()) {
                binding.price.text = it
            } else {
                (activity as? MainActivity)?.setActionBarSubTitle(it)
            }
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
                )
            )

            navigate(
                R.id.exportWalletFragment,
                bundle
            )
        }

        binding.withdraw.onClick {
            navigate(
                R.id.withdrawalTypeFragment,
                Constants.Navigation.ANIMATED_SLIDE_UP_OPTION
            )
        }
    }

    companion object {
        fun showBasicInfoBottomSheet(
            context: Context,
            title: String,
            message: String,
            addToStack: (AppCompatDialog) -> Unit,
            removeFromStack: (AppCompatDialog) -> Unit
        ) {
            val bottomSheetDialog = BottomSheetDialog(context)
            addToStack(bottomSheetDialog)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_basic_info)
            val _title = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)!!
            val _message = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_message)!!
            val close = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.close)!!
            _title.text = title
            _message.text = message

            close.onClick {
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.setOnCancelListener {
                removeFromStack(bottomSheetDialog)
            }
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetDialog.show()
        }
    }

    fun getConfirmationsForTransaction(transaction: TransactionInfo) : Int {
        return viewModel.getConfirmations(transaction)
    }

    fun onTransactionSelected(transaction: TransactionInfo) {
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
        if(viewModel.getWallet() != null && wallet.uuid == viewModel.getWalletId()
            && activity != null && _binding != null) {
            val state = wallet.onWalletStateChanged(
                requireContext(),
                wallet.syncPercentage,
                false,
                localStoreRepository
            )

            if(localStoreRepository.isProEnabled()) {
                binding.price.text = state
            } else {
                (activity as? MainActivity)?.setActionBarSubTitle(state)
            }

            if (wallet.isSynced && wallet.isSynced)
                viewModel.getTransactions()
            binding.swipeContainer.isRefreshing = wallet.isSyncing
        }
    }

    override fun onWalletAlreadySynced(wallet: LocalWalletModel) {
        if(viewModel.getWallet() != null && wallet.uuid == viewModel.getWalletId() && activity != null && _binding != null) {
            binding.swipeContainer.isRefreshing = false
        }
    }

    override fun onNavigateTo(destination: Int) {
        navigate(destination)
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.walletProDashboardFragment
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
            R.id.walletSettingsFragment
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeWalletSyncListener(this)
        _binding = null
    }

}
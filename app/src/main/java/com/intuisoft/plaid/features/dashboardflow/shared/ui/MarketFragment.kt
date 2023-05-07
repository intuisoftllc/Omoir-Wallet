package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventMarketView
import com.intuisoft.plaid.delegates.DelegateManager
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.databinding.FragmentMarketBinding
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.BasicLineChartAdapter
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.MarketViewModel
import com.intuisoft.plaid.common.util.SimpleTimeFormat
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class MarketFragment : ConfigurableFragment<FragmentMarketBinding>(pinProtection = true) {
    private val viewModel: MarketViewModel by viewModel()
    private val localStore: LocalStoreRepository by inject()
    private val delegateManager: DelegateManager by inject()
    protected val eventTracker: EventTracker by inject()
    protected val billingManager: BillingManager by inject()

    val adapter = BasicLineChartAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMarketBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())

        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        eventTracker.log(EventMarketView())
        binding.sparkview.setAdapter(adapter)
        binding.sparkview.isScrubEnabled = true
        binding.sparkview.setScrubListener {

            if(it != null) {
                val data = it as ChartDataModel
                binding.percentageGain.visibility = View.INVISIBLE
                binding.scrubTime.visibility = View.VISIBLE
                binding.price.text = SimpleCurrencyFormat.formatValue(
                    localStore.getLocalCurrency(),
                    data.value.toDouble()
                )

                binding.scrubTime.text = SimpleTimeFormat.getDateByLocale(data.time, Locale.US)
            } else {
                binding.percentageGain.visibility = View.VISIBLE
                binding.scrubTime.visibility = View.INVISIBLE
                viewModel.setTickerPrice()
            }
        }

        binding.interval1day.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_MED) {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_1DAY)
            binding.interval1day.selectTimePeriod(true)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.interval1week.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_MED) {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_1WEEK)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(true)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.interval1Month.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_MED) {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_1MONTH)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(true)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.interval3Month.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_MED) {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_3MONTHS)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(true)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.interval6Month.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_MED) {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_6MONTHS)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(true)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.interval1Year.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_MED) {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_1YEAR)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(true)
            binding.intervalMax.selectTimePeriod(false)
        }

        binding.intervalMax.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_MED) {
            viewModel.changeChartInterval(ChartIntervalType.INTERVAL_ALL_TIME)
            binding.interval1day.selectTimePeriod(false)
            binding.interval1week.selectTimePeriod(false)
            binding.interval1Month.selectTimePeriod(false)
            binding.interval3Month.selectTimePeriod(false)
            binding.interval6Month.selectTimePeriod(false)
            binding.interval1Year.selectTimePeriod(false)
            binding.intervalMax.selectTimePeriod(true)
        }

        viewModel.percentageGain.observe(viewLifecycleOwner, Observer {
            binding.percentageGain.text = SimpleCoinNumberFormat.formatCurrency(it) + "%"

            if(it >= 0.0) {
                binding.percentageGain.setTextColor(resources.getColor(R.color.success_color))
            } else {
                binding.percentageGain.setTextColor(resources.getColor(R.color.alt_error_color))
            }
        })

        viewModel.maxSupply.observe(viewLifecycleOwner, Observer {
            binding.maxSupply.text = it
        })

        viewModel.circulatingSupply.observe(viewLifecycleOwner, Observer {
            binding.circulatingSupply.text = it
        })

        viewModel.marketCap.observe(viewLifecycleOwner, Observer {
            binding.marketCap.text = it
        })

        viewModel.totalVolume.observe(viewLifecycleOwner, Observer {
            binding.volume.text = it
        })

        viewModel.blockStatsDataLoading.observe(viewLifecycleOwner, Observer {
            binding.extendedNetworkDataLoading.isVisible = it
        })

        viewModel.basicNetworkDataLoading.observe(viewLifecycleOwner, Observer {
            binding.basicNetworkDataLoading.isVisible = it
        })

        viewModel.blockStatsTitles.observe(viewLifecycleOwner, Observer {
            binding.blockStats1Title.text = it[0]
            binding.blockStats2Title.text = it[1]
            binding.blockStats3Title.text = it[2]
            binding.blockStats4Title.text = it[3]
            binding.blockStats5Title.text = it[4]
            binding.blockStats6Title.text = it[5]
            binding.blockStats7Title.text = it[6]
            binding.blockStats8Title.text = it[7]
            binding.blockStats9Title.text = it[8]
            binding.blockStats10Title.text = it[9]
            binding.blockStats11Title.text = it[10]
        })

        viewModel.blockStatsSubTitles.observe(viewLifecycleOwner, Observer {
            binding.blockStats1Subtitle.text = it[0].second
            binding.blockStats2Subtitle.text = it[1].second
            binding.blockStats3Subtitle.text = it[2].second
            binding.blockStats4Subtitle.text = it[3].second
            binding.blockStats5Subtitle.text = it[4].second
            binding.blockStats6Subtitle.text = it[5].second
            binding.blockStats7Subtitle.text = it[6].second
            binding.blockStats8Subtitle.text = it[7].second
            binding.blockStats9Subtitle.text = it[8].second
            binding.blockStats10Subtitle.text = it[9].second
            binding.blockStats11Subtitle.text = it[10].second
        })

        viewModel.upgradeToPro.observe(viewLifecycleOwner, Observer {
            binding.upgradeToProContainer.isVisible = it

            if(!it) {
                binding.blockStats1Subtitle.text = ""
                binding.blockStats2Subtitle.text = ""
                binding.blockStats3Subtitle.text = ""
                binding.blockStats4Subtitle.text = ""
                binding.blockStats5Subtitle.text = ""
                binding.blockStats6Subtitle.text = ""
                binding.blockStats7Subtitle.text = ""
                binding.blockStats8Subtitle.text = ""
                binding.blockStats9Subtitle.text = ""
                binding.blockStats10Subtitle.text = ""
                binding.blockStats11Subtitle.text = ""
            }
        })

        binding.upgrade.onClick {
            binding.upgrade.enableButton(false)
            billingManager.shouldShowPremiumContent { hasSubscription ->
                if(!hasSubscription) {
                    navigate(
                        R.id.purchaseSubscriptionFragment,
                        Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
                    )
                } else {
                    withBinding {
                        upgrade.enableButton(true)
                        viewModel.checkProStatus()
                        viewModel.updateData()
                    }
                }
            }
        }

        binding.tokenDescription.text = delegateManager.current().marketDelegate.getTickerDescription()
        binding.tokenDescription.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
            openLink(delegateManager.current().marketDelegate.learnMoreLink)
        }

        binding.website.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
            openLink(delegateManager.current().marketDelegate.website)
        }

        binding.explorer.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
            openLink(delegateManager.current().networkDelegate.explorer)
        }

        binding.marketData.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
            openLink(delegateManager.current().marketDelegate.coingeckoLink)
        }

        viewModel.couldNotLoadData.observe(viewLifecycleOwner, Observer {
            binding.chartDataLoading.isVisible = false
            styledSnackBar(requireView(), getString(R.string.market_data_load_error), true)
        })

        viewModel.tickerPrice.observe(viewLifecycleOwner, Observer {
            binding.price.text = it
        })

        viewModel.chartData.observe(viewLifecycleOwner, Observer {
            adapter.setItems(it)
        })

        viewModel.showContent.observe(viewLifecycleOwner, Observer {
            activateNoInternet(!it)
        })
    }

    override fun onBackPressed() {
        onNavigateBottomBarSecondaryFragmentBackwards()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkProStatus()
        viewModel.updateData()
    }

    override fun onNetworkStateChanged(hasNetwork: Boolean) {
        viewModel.onNoInternet(hasNetwork)
    }

    override fun onNavigateTo(destination: Int) {
        navigate(destination)
    }

    override fun showBottomBar(): Boolean {
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun onActionLeft() {
    }

    override fun actionBarSubtitle(): Int {
        return R.string.market_fragment_label
    }

    override fun navigationId(): Int {
        return R.id.marketFragment
    }
}
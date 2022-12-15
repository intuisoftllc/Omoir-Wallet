package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.model.ChartDataModel
import com.intuisoft.plaid.common.model.ChartIntervalType
import com.intuisoft.plaid.common.model.CongestionRating
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.databinding.FragmentMarketBinding
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.BasicLineChartAdapter
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.MarketViewModel
import com.intuisoft.plaid.util.SimpleTimeFormat
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class MarketFragment : ConfigurableFragment<FragmentMarketBinding>(pinProtection = true) {
    private val viewModel: MarketViewModel by viewModel()
    private val localStore: LocalStoreRepository by inject()

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
        onBackPressedCallback {
            onNavigateBottomBarSecondaryFragmentBackwards(localStore)
        }

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

        viewModel.percentageGain.observe(viewLifecycleOwner, Observer {
            binding.percentageGain.text = SimpleCoinNumberFormat.formatCurrency(it) + "%"

            if(it > 0.0) {
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

        viewModel.volume24Hr.observe(viewLifecycleOwner, Observer {
            binding.volume.text = it
        })

        viewModel.nodesOnNetwork.observe(viewLifecycleOwner, Observer {
            binding.nodes.text = it
        })

        viewModel.memoryPoolSize.observe(viewLifecycleOwner, Observer {
            binding.memPoolSize.text = it
        })

        viewModel.txPerSecond.observe(viewLifecycleOwner, Observer {
            binding.txsPerSecond.text = it
        })

        viewModel.addressesWithBalance.observe(viewLifecycleOwner, Observer {
            binding.addressesWithBalances.text = it
        })

        viewModel.checkProStatus()
        viewModel.upgradeToPro.observe(viewLifecycleOwner, Observer {
            binding.upgradeToProContainer.isVisible = it

            if(!it) {
                binding.height.text = ""
                binding.difficulty.text = ""
                binding.blockchainSize.text = ""
                binding.unconfirmedTxs.text = ""
                binding.avgConfTime.text = ""
                binding.congestionRating.text = ""
                binding.nodes.text = ""
                binding.memPoolSize.text = ""
                binding.txsPerSecond.text = ""
                binding.addressesWithBalances.text = ""
            }
        })

        viewModel.congestionRating.observe(viewLifecycleOwner, Observer {
            when(it) {
                CongestionRating.NA -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.text_grey))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_6)
                }
                CongestionRating.LIGHT -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.success_color))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_1)
                }
                CongestionRating.NORMAL -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.text_grey))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_2)
                }
                CongestionRating.MED -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.warning_color))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_3)
                }
                CongestionRating.BUSY -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.warning_color))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_4)
                }
                CongestionRating.CONGESTED -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.error_color))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_5)
                }
            }
        })

        binding.bitcoinDescription.setOnClickListener {
            openLink(getString(R.string.market_data_what_is_bitcoin_link))
        }

        binding.bitcoinOrg.setOnClickListener {
            openLink(getString(R.string.market_data_bitcoin_org_link))
        }

        binding.explorer.setOnClickListener {
            openLink(getString(R.string.market_data_bitcoin_explorer_link))
        }

        binding.marketData.setOnClickListener {
            openLink(getString(R.string.market_data_bitcoin_market_external_link))
        }

        viewModel.couldNotLoadData.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), getString(R.string.market_data_load_error), true)
        })

        viewModel.network.observe(viewLifecycleOwner, Observer {
            binding.network.text = it
        })

        viewModel.blockHeight.observe(viewLifecycleOwner, Observer {
            binding.height.text = it
        })

        viewModel.difficulty.observe(viewLifecycleOwner, Observer {
            binding.difficulty.text = it
        })

        viewModel.blockchainSize.observe(viewLifecycleOwner, Observer {
            binding.blockchainSize.text = it
        })

        viewModel.unconfirmedTxs.observe(viewLifecycleOwner, Observer {
            binding.unconfirmedTxs.text = it
        })

        viewModel.avgConfTime.observe(viewLifecycleOwner, Observer {
            binding.avgConfTime.text = it
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

    override fun onResume() {
        super.onResume()
        viewModel.updateData()
    }

    override fun onNetworkStateChanged(hasNetwork: Boolean) {
        viewModel.onNoInternet(hasNetwork)
    }

    override fun onNavigateTo(destination: Int) {
        navigate(destination)
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
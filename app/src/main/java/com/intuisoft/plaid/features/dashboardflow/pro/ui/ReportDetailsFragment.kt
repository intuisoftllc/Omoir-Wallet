package com.intuisoft.plaid.features.dashboardflow.pro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.model.ReportHistoryTimeFilter
import com.intuisoft.plaid.common.model.ReportType
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.FragmentReportDetailsBinding
import com.intuisoft.plaid.features.dashboardflow.pro.viewmodel.ReportDetailsViewModel
import com.intuisoft.plaid.features.homescreen.adapters.BasicTransactionAdapter
import com.intuisoft.plaid.common.util.Plural
import com.intuisoft.plaid.common.util.SimpleTimeFormat
import com.intuisoft.plaid.util.fragmentconfig.BasicConfigData
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReportDetailsFragment : ConfigurableFragment<FragmentReportDetailsBinding>(pinProtection = true, premiumContent = true) {
    protected val viewModel: ReportDetailsViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()

    private val adapter = BasicTransactionAdapter(
        onTransactionSelected = ::onTransactionSelected,
        getConfirmationsForTransaction = ::getConfirmationsForTransaction,
        localStoreRepository = localStoreRepository
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentReportDetailsBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_INFLOW_REPORT,
                FragmentConfigurationType.CONFIGURATION_OUTFLOW_REPORT,
                FragmentConfigurationType.CONFIGURATION_FEE_REPORT
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        configuration?.let {
            when(configuration.configurationType) {
                FragmentConfigurationType.CONFIGURATION_INFLOW_REPORT -> {
                    viewModel.type = ReportType.INFLOW_REPORT
                    binding.totalTitle.text = getString(R.string.report_details_report_type_inflow_title)
                }

                FragmentConfigurationType.CONFIGURATION_OUTFLOW_REPORT -> {
                    viewModel.type = ReportType.OUTFLOW_REPORT
                    binding.totalTitle.text = getString(R.string.report_details_report_type_outflow_title)
                }

                FragmentConfigurationType.CONFIGURATION_FEE_REPORT -> {
                    viewModel.type = ReportType.FEE_REPORT
                    binding.totalTitle.text = getString(R.string.report_details_report_type_fee_title)
                }
            }
        }

        viewModel.setupSubscriptions()
        viewModel.barData.observe(viewLifecycleOwner, Observer {
            if(it != null) {
                when(it.items.size) {
                    BarChartSizeConfiguration.CHART_SIZE_SMALL.barCount -> {
                        binding.chart.sizeConfig = BarChartSizeConfiguration.CHART_SIZE_SMALL
                    }

                    BarChartSizeConfiguration.CHART_SIZE_MEDIUM.barCount -> {
                        binding.chart.sizeConfig = BarChartSizeConfiguration.CHART_SIZE_MEDIUM
                    }

                    BarChartSizeConfiguration.CHART_SIZE_MEDIUM_LARGE.barCount -> {
                        binding.chart.sizeConfig = BarChartSizeConfiguration.CHART_SIZE_MEDIUM_LARGE
                    }

                    BarChartSizeConfiguration.CHART_SIZE_LARGE.barCount -> {
                        binding.chart.sizeConfig = BarChartSizeConfiguration.CHART_SIZE_LARGE
                    }

                    else -> {
                        binding.chart.sizeConfig = BarChartSizeConfiguration.CHART_SIZE_SMALL
                    }
                }

                binding.chart.data =
                    it.items.map { item ->
                        item.barName to item.value.from(RateConverter.RateType.SATOSHI_RATE).toFloat()
                    }.toArrayList()

                viewModel.enableContent()
            } else {
                binding.chart.sizeConfig = BarChartSizeConfiguration.CHART_SIZE_SMALL
                binding.chart.data = listOf()
            }
        })

        binding.chart.listener = object : BarSelectedListener {
            override fun onSelected(index: Int) {
                viewModel.onBarSelected(true, index)
            }

            override fun onDeSelected(index: Int) {
                viewModel.onBarSelected(false, index)
            }

        }

        viewModel.currentTimePeriod.observe(viewLifecycleOwner, Observer { (start, end) ->
            binding.timePeriodStart.text = SimpleTimeFormat.fullDateShort(start)
            binding.timePeriodEnd.text = SimpleTimeFormat.fullDateShort(end)
        })

        viewModel.gettingData.observe(viewLifecycleOwner, Observer {
            binding.loading.isVisible = it
        })

        viewModel.total.observe(viewLifecycleOwner, Observer {
            binding.totalValue.text = it
        })

        binding.timePeriod.onClick {
            showTimePeriodSelectionSheet()
        }

        viewModel.transactions.observe(viewLifecycleOwner, Observer {
            binding.noTransactionsIcon.isVisible = it.isEmpty()
            binding.noTransactionsMessage.isVisible = it.isEmpty()
            binding.transactions.isVisible = it.isNotEmpty()

            binding.timePeriodValue.text = Plural.of("Transaction", it.size.toLong(), "s")
            adapter.addTransactions(it.toArrayList())
            binding.transactions.adapter = adapter
        })

        viewModel.onFilterUpdate.observe(viewLifecycleOwner, Observer {
            when(it) {
                ReportHistoryTimeFilter.LAST_WEEK -> {
                    binding.timePeriod.setButtonText(getString(R.string.report_details_time_period_1))
                }
                ReportHistoryTimeFilter.LAST_MONTH -> {
                    binding.timePeriod.setButtonText(getString(R.string.report_details_time_period_2))
                }
                ReportHistoryTimeFilter.LAST_6MONTHS -> {
                    binding.timePeriod.setButtonText(getString(R.string.report_details_time_period_3))
                }
                ReportHistoryTimeFilter.LAST_YEAR -> {
                    binding.timePeriod.setButtonText(getString(R.string.report_details_time_period_4))
                }
                ReportHistoryTimeFilter.ALL_TIME -> {
                    binding.timePeriod.setButtonText(getString(R.string.report_details_time_period_5))
                }

                else -> {
                    binding.timePeriod.setButtonText(getString(R.string.report_details_time_period_1))
                }
            }
        })

        binding.totalValue.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
            viewModel.changeDisplayUnit()
        }

        viewModel.contentEanbled.observe(viewLifecycleOwner, Observer {
            binding.timePeriod.enableButton(it)
            binding.totalValue.isClickable = it
        })

        viewModel.noData.observe(viewLifecycleOwner, Observer {
            binding.noData.isVisible = it
        })
    }

    private fun showTimePeriodSelectionSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        addToStack(bottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_reports_filter)
        val lastWeek = bottomSheetDialog.findViewById<SettingsItemView>(R.id.last_week)!!
        val lastMonth = bottomSheetDialog.findViewById<SettingsItemView>(R.id.last_month)!!
        val last6Months = bottomSheetDialog.findViewById<SettingsItemView>(R.id.last_6months)!!
        val lastYear = bottomSheetDialog.findViewById<SettingsItemView>(R.id.last_year)!!
        val allTime = bottomSheetDialog.findViewById<SettingsItemView>(R.id.all_time)!!

        when(viewModel.getFilter()) {
            ReportHistoryTimeFilter.LAST_WEEK -> {
                lastWeek.checkRadio(true)
            }
            ReportHistoryTimeFilter.LAST_MONTH -> {
                lastMonth.checkRadio(true)
            }
            ReportHistoryTimeFilter.LAST_6MONTHS -> {
                last6Months.checkRadio(true)
            }
            ReportHistoryTimeFilter.LAST_YEAR -> {
                lastYear.checkRadio(true)
            }
            ReportHistoryTimeFilter.ALL_TIME -> {
                allTime.checkRadio(true)
            }
        }

        lastWeek.onRadioClicked { view, clicked ->
            if(clicked) {
                viewModel.setFilter(ReportHistoryTimeFilter.LAST_WEEK)
                lastMonth.checkRadio(false)
                last6Months.checkRadio(false)
                lastYear.checkRadio(false)
                allTime.checkRadio(false)
            }
        }

        lastMonth.onRadioClicked { view, clicked ->
            if(clicked) {
                viewModel.setFilter(ReportHistoryTimeFilter.LAST_MONTH)
                lastWeek.checkRadio(false)
                last6Months.checkRadio(false)
                lastYear.checkRadio(false)
                allTime.checkRadio(false)
            }
        }

        last6Months.onRadioClicked { view, clicked ->
            if(clicked) {
                viewModel.setFilter(ReportHistoryTimeFilter.LAST_6MONTHS)
                lastMonth.checkRadio(false)
                lastWeek.checkRadio(false)
                lastYear.checkRadio(false)
                allTime.checkRadio(false)
            }
        }

        lastYear.onRadioClicked { view, clicked ->
            if(clicked) {
                viewModel.setFilter(ReportHistoryTimeFilter.LAST_YEAR)
                lastMonth.checkRadio(false)
                last6Months.checkRadio(false)
                lastWeek.checkRadio(false)
                allTime.checkRadio(false)
            }
        }

        allTime.onRadioClicked { view, clicked ->
            if(clicked) {
                viewModel.setFilter(ReportHistoryTimeFilter.ALL_TIME)
                lastMonth.checkRadio(false)
                last6Months.checkRadio(false)
                lastYear.checkRadio(false)
                lastWeek.checkRadio(false)
            }
        }

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
        }
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
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

    override fun onNavigateTo(destination: Int) {
        navigate(destination)
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.reportDetailsFragment
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun actionBarSubtitle(): Int {
        return when {
            viewModel.hasConfiguration(FragmentConfigurationType.CONFIGURATION_OUTFLOW_REPORT) -> {
                R.string.report_details_outflow_title
            }

            viewModel.hasConfiguration(FragmentConfigurationType.CONFIGURATION_INFLOW_REPORT) -> {
                R.string.report_details_inflow_title
            }

            viewModel.hasConfiguration(FragmentConfigurationType.CONFIGURATION_FEE_REPORT) -> {
                R.string.report_details_fee_title
            }

            else -> {
                R.string.report_details_fee_title
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.intuisoft.plaid.features.dashboardflow.pro.ui

import android.Manifest
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventWalletSettingsExportCsv
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants.Time.MIN_CLICK_INTERVAL_SHORT
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.common.util.SimpleTimeFormat
import com.intuisoft.plaid.databinding.FragmentExportOptionsBinding
import com.intuisoft.plaid.features.dashboardflow.pro.viewmodel.ExportOptionsViewModel
import com.intuisoft.plaid.features.dashboardflow.shared.ui.WalletExportFragment
import com.intuisoft.plaid.model.ExportDataType
import com.intuisoft.plaid.model.ValueFilter
import kotlinx.android.synthetic.main.custom_view_settings_item.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Instant
import java.util.*

class ExportOptionsFragment : ConfigurableFragment<FragmentExportOptionsBinding>(pinProtection = true, premiumContent = true) {
    protected val viewModel: ExportOptionsViewModel by viewModel()
    protected val eventTracker: EventTracker by inject()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val billing: BillingManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentExportOptionsBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.pdfExport.enableButton(false)
        viewModel.setStartAndEndPeriod()
        binding.csvExport.onClick {
            requireActivity().checkAppPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 100) {
                showExportOptionsDialog()
            }
        }

        viewModel.exportFinished.observe(viewLifecycleOwner, Observer {
            activateAnimatedLoading(false, "")

            if(it != null) {
                styledSnackBar(requireView(), getString(R.string.export_data_finished_message, it))
            } else {
                styledSnackBar(requireView(), getString(R.string.export_data_failed_message))
            }
        })
    }

    private fun exportData() {
      eventTracker.log(EventWalletSettingsExportCsv())
      activateAnimatedLoading(true, getString(R.string.export_data_loading_message))
      viewModel.exportToCsv()
    }

    fun showExportOptionsDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        addToStack(bottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_export_options)
        val mode = bottomSheetDialog.findViewById<Switch>(R.id.export_mode)!!
        val sats = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.sats)!!
        val btc = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.btc)!!
        val fiat = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.currency)!!
        val valueAmountTitle = bottomSheetDialog.findViewById<TextView>(R.id.value_amount_title)!!
        val valueAmountContainer = bottomSheetDialog.findViewById<CardView>(R.id.value_amount_container)!!
        val startDate = bottomSheetDialog.findViewById<TextView>(R.id.start_date)!!
        val endDate = bottomSheetDialog.findViewById<TextView>(R.id.end_date)!!
        val dataType = bottomSheetDialog.findViewById<SettingsItemView>(R.id.export_data_type)!!
        val valueFilterIcon = bottomSheetDialog.findViewById<ImageView>(R.id.data_filter_icon)!!
        val dataValue = bottomSheetDialog.findViewById<EditText>(R.id.data_value)!!
        val done = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.done)!!
        val rateConverter = RateConverter(localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0 )

        mode.isEnabled = false
        billing.shouldShowPremiumContent {
            mode.isEnabled = it
        }
        mode.isChecked = viewModel.advancedExport
        valueAmountTitle.isVisible = viewModel.advancedExport
        valueAmountContainer.isVisible = viewModel.advancedExport
        dataType.isVisible = viewModel.advancedExport
        valueFilterIcon.setImageResource(viewModel.valueFilter.displayIcon)
        dataType.setSubTitleText(getString(viewModel.dataType.displayName))
        fiat.setButtonText(SimpleCurrencyFormat.getSymbol(localStoreRepository.getLocalCurrency()))

        val onDisplayUnitUpdated: () -> Unit = {
            when(localStoreRepository.getBitcoinDisplayUnit()) {
                BitcoinDisplayUnit.SATS -> {
                    sats.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                    btc.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    fiat.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    dataValue.setText(
                        rateConverter.from(
                            RateConverter.RateType.SATOSHI_RATE,
                            localStoreRepository.getLocalCurrency(),
                            false
                        ).first
                    )
                }

                BitcoinDisplayUnit.BTC -> {
                    sats.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    btc.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                    fiat.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    dataValue.setText(
                        rateConverter.from(
                            RateConverter.RateType.BTC_RATE,
                            localStoreRepository.getLocalCurrency(),
                            false
                        ).first
                    )
                }

                BitcoinDisplayUnit.FIAT -> {
                    sats.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    btc.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    fiat.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                    dataValue.setText(
                        rateConverter.from(
                            RateConverter.RateType.FIAT_RATE,
                            localStoreRepository.getLocalCurrency(),
                            false
                        ).first
                    )
                }
            }
        }

        val onValueFilterUpdated: () -> Unit = {
            valueFilterIcon.setImageResource(viewModel.valueFilter.displayIcon)
        }

        val updateValueFilter: () -> Unit = {
            when(viewModel.valueFilter) {
                ValueFilter.LESS_THAN -> {
                    viewModel.valueFilter = ValueFilter.GREATER_THAN
                }

                ValueFilter.GREATER_THAN -> {
                    viewModel.valueFilter = ValueFilter.GREATER_THAN_EQ
                }

                ValueFilter.GREATER_THAN_EQ -> {
                    viewModel.valueFilter = ValueFilter.LESS_THAN_EQ
                }

                ValueFilter.LESS_THAN_EQ -> {
                    viewModel.valueFilter = ValueFilter.EQUAL_TO
                }

                ValueFilter.EQUAL_TO -> {
                    viewModel.valueFilter = ValueFilter.NOT_EQUAL_TO
                }

                ValueFilter.NOT_EQUAL_TO -> {
                    viewModel.valueFilter = ValueFilter.LESS_THAN
                }
            }

            onValueFilterUpdated()
        }

        val onTimePeriodsUpdated: () -> Unit = {
            startDate.text = SimpleTimeFormat.fullDateShort(viewModel.startPeriod)
            endDate.text = SimpleTimeFormat.fullDateShort(viewModel.endPeriod)
        }

        onDisplayUnitUpdated()
        onTimePeriodsUpdated()
        onValueFilterUpdated()
        WalletExportFragment.addDisplayUnitBasedTextListener(
            dataValue,
            localStoreRepository,
            rateConverter
        ) {
            viewModel.dataValueLimit = rateConverter.getRawBtcRate()
        }

        mode.setOnCheckedChangeListener { view, isChecked ->

            viewModel.advancedExport = isChecked
            valueAmountTitle.isVisible = isChecked
            valueAmountContainer.isVisible = isChecked
            dataType.isVisible = isChecked
        }

        sats.onClick {
            localStoreRepository.updateBitcoinDisplayUnit(BitcoinDisplayUnit.SATS)
            onDisplayUnitUpdated()
        }

        btc.onClick {
            localStoreRepository.updateBitcoinDisplayUnit(BitcoinDisplayUnit.BTC)
            onDisplayUnitUpdated()
        }

        fiat.onClick {
            localStoreRepository.updateBitcoinDisplayUnit(BitcoinDisplayUnit.FIAT)
            onDisplayUnitUpdated()
        }

        startDate.setOnSingleClickListener {
            showDatePicker(
                initialTimeMills = viewModel.startPeriod.toEpochMilli(),
                minTimeMills = viewModel.getWalletBirthday(),
                maxTimeMills = viewModel.endPeriod.toEpochMilli()
            ) {
                viewModel.startPeriod = Instant.ofEpochMilli(it)
                onTimePeriodsUpdated()
            }
        }

        endDate.setOnSingleClickListener {
            showDatePicker(
                initialTimeMills = viewModel.endPeriod.toEpochMilli(),
                minTimeMills = viewModel.startPeriod.toEpochMilli(),
                maxTimeMills = Instant.now().toEpochMilli()
            ) {
                viewModel.endPeriod = Instant.ofEpochMilli(it)
                onTimePeriodsUpdated()
            }
        }

        valueFilterIcon.setOnSingleClickListener(MIN_CLICK_INTERVAL_SHORT) {
            updateValueFilter()
        }

        dataType.onClick {
            bottomSheetDialog.cancel()
            showTransactionTypeDialog()
        }

        done.onClick {
            bottomSheetDialog.cancel()
            exportData()
        }

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
        }
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }

    private fun showDatePicker(
        initialTimeMills: Long,
        minTimeMills: Long,
        maxTimeMills: Long,
        onTimeSelected: (Long) -> Unit
    ) {
        val mCalendar = Calendar.getInstance()
        mCalendar.timeInMillis = initialTimeMills

        val mDialog = DatePickerDialog(requireContext(), { _, mYear, mMonth, mDay ->
            mCalendar[Calendar.YEAR] = mYear
            mCalendar[Calendar.MONTH] = mMonth
            mCalendar[Calendar.DAY_OF_MONTH] = mDay
            onTimeSelected(mCalendar.timeInMillis)
        }, mCalendar[Calendar.YEAR], mCalendar[Calendar.MONTH], mCalendar[Calendar.DAY_OF_MONTH])
        addToStack(mDialog)


        mCalendar.timeInMillis = minTimeMills
        mDialog.datePicker.minDate = mCalendar.timeInMillis

        mCalendar.timeInMillis = maxTimeMills
        mDialog.datePicker.maxDate = mCalendar.timeInMillis

        mDialog.setOnCancelListener {
            removeFromStack(mDialog)
        }

        // Display the calendar dialog
        mDialog.show()
    }

    fun showTransactionTypeDialog() {

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        var ignoreCancel = false
        addToStack(bottomSheetDialog) {
            ignoreCancel = true
        }
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_data_type_filter)
        val raw = bottomSheetDialog.findViewById<SettingsItemView>(R.id.transaction_type_raw)!!
        val incoming = bottomSheetDialog.findViewById<SettingsItemView>(R.id.transaction_type_incoming)!!
        val outgoing = bottomSheetDialog.findViewById<SettingsItemView>(R.id.transaction_type_outgoing)!!
        val done = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.done)!!


        val onDataTypeUpdated: () -> Unit = {
            raw.checkRadio(viewModel.dataType == ExportDataType.RAW)
            incoming.checkRadio(viewModel.dataType == ExportDataType.INCOMING)
            outgoing.checkRadio(viewModel.dataType == ExportDataType.OUTGOING)
        }

        onDataTypeUpdated()
        raw.onClick {
            viewModel.dataType = ExportDataType.RAW
            onDataTypeUpdated()
        }

        incoming.onClick {
            viewModel.dataType = ExportDataType.INCOMING
            onDataTypeUpdated()
        }

        outgoing.onClick {
            viewModel.dataType = ExportDataType.OUTGOING
            onDataTypeUpdated()
        }

        done.onClick {
            bottomSheetDialog.cancel()
        }

        bottomSheetDialog.setOnCancelListener {
            if(!ignoreCancel) {
                removeFromStack(bottomSheetDialog)
                showExportOptionsDialog()
            }
        }

        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.exportOptionsFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
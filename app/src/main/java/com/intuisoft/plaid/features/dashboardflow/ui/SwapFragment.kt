package com.intuisoft.plaid.features.dashboardflow.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.common.util.extensions.containsNumbers
import com.intuisoft.plaid.databinding.FragmentSwapBinding
import com.intuisoft.plaid.features.dashboardflow.viewmodel.SwapViewModel
import com.intuisoft.plaid.util.NetworkUtil
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class SwapFragment : PinProtectedFragment<FragmentSwapBinding>() {
    private val viewModel: SwapViewModel by viewModel()
    private val localStore: LocalStoreRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSwapBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())

        return binding.root
    }


    override fun onConfiguration(configuration: FragmentConfiguration?) {

        onBackPressedCallback {
            onNavigateBottomBarSecondaryFragmentBackwards(localStore)
        }

        viewModel.setInitialValues()
        binding.swapPairSend.setOnTextChangedListener {
            viewModel.validateSendAmount(
                if(it?.isNotEmpty() == true && it.containsNumbers())
                    it.toDouble()
                else 0.0
            )
        }

        viewModel.sendPairInfo.observe(viewLifecycleOwner, Observer {
            if(it.ticker.lowercase() == "btc") {
                binding.swapPairSend.setTickerSymbol(R.drawable.ic_bitcoin)
            } else if(it.symbol != null)
                binding.swapPairSend.setTickerSymbol(it.symbol)
            else binding.swapPairSend.setTickerSymbol(0)

            binding.swapPairSend.setPairTitle(it.pairSendReciveTitle)
            binding.swapPairSend.setStyle(it.pairType)
            binding.swapPairSend.setTicker(it.ticker)
            binding.swapPairSend.setValue(it.receiveValue)
        })

        binding.swapSendReceive.setOnClickListener {
            viewModel.swapSendReceive()
        }

        viewModel.onDisplayExplanation.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), it, true)
        })

        viewModel.confirmButtonEnabled.observe(viewLifecycleOwner, Observer {
            binding.confirm.enableButton(it)
        })

        binding.confirm.onClick {
            binding.confirm.enableButton(false)
            viewModel.onNext()
        }

        viewModel.recievePairInfo.observe(viewLifecycleOwner, Observer {
            if(it.ticker.lowercase() == "btc") {
                binding.swapPairReceive.setTickerSymbol(R.drawable.ic_bitcoin)
            } else if(it.symbol != null)
                binding.swapPairReceive.setTickerSymbol(it.symbol)
            else binding.swapPairReceive.setTickerSymbol(0)

            binding.swapPairReceive.setPairTitle(it.pairSendReciveTitle)
            binding.swapPairReceive.setStyle(it.pairType)
            binding.swapPairReceive.setTicker(it.ticker)
            binding.swapPairReceive.setValue(it.receiveValue)
        })

        binding.fixed.onClick {
            viewModel.setFixed(true)
        }

        viewModel.fixedRange.observe(viewLifecycleOwner, Observer {
            setMinMax(it)
            binding.fixed.setButtonStyle(RoundedButtonView.ButtonStyle.ROUNDED_STYLE)
            binding.floating.setButtonStyle(RoundedButtonView.ButtonStyle.OUTLINED_STYLE)
        })
        
        viewModel.floatingRange.observe(viewLifecycleOwner, Observer {
            setMinMax(it)
            binding.fixed.setButtonStyle(RoundedButtonView.ButtonStyle.OUTLINED_STYLE)
            binding.floating.setButtonStyle(RoundedButtonView.ButtonStyle.ROUNDED_STYLE)
        })

        binding.floating.onClick {
            viewModel.setFixed(false)
        }

        viewModel.screenFunctionsEnabled.observe(viewLifecycleOwner, Observer {
            binding.swapSendReceive.isClickable = it
            binding.fixed.enableButton(it)
            binding.floating.enableButton(it)
            binding.swapPairSend.setTickerClickable(it)
            binding.swapPairReceive.setTickerClickable(it)
        })

        viewModel.conversionAmount.observe(viewLifecycleOwner, Observer {
            binding.swapPairReceive.setValue(it)
        })

        viewModel.getReceiveAddress.observe(viewLifecycleOwner, Observer {
            onGetReceiveAddress(it.first, it.second)
        })

        viewModel.getRefundAddress.observe(viewLifecycleOwner, Observer {
            onGetRefundAddress(it.first, it.second)
        })

        viewModel.showContent.observe(viewLifecycleOwner, Observer {
            binding.screenContents.isVisible = it
            binding.noInternet.isVisible = !it
        })

        viewModel.exchangeInfoDisplay.observe(viewLifecycleOwner, Observer {
            confirmExchange(it)
        })
    }

    private fun setMinMax(minMax: Pair<String, String>?) {
        binding.minMaxContainer.isVisible = minMax != null
        binding.minMaxTitle.isVisible = minMax != null

        minMax?.let {
            binding.min.text = it.first
            binding.max.text = it.second
        }
    }

    private fun setReceiveAddress(address: String, memo: String) {
        viewModel.setReceiveAddress(address, memo)
    }

    private fun setRefundAddress(address: String, memo: String) {
        viewModel.setRefundAddress(address, memo)
    }

    private fun onGetReceiveAddress(ticker: String, validationRegex: Pair<String, String?>) {
        enterAddressDialog(
            context = requireContext(),
            title = getString(R.string.swap_deposit_address_title),
            ticker = ticker,
            depositAddressTitle = getString(R.string.swap_deposit_address_entry_title, ticker),
            addressValidationRegex = validationRegex.first,
            memoValidationRegex = validationRegex.second,
            setAddress = ::setReceiveAddress
        )
    }

    private fun onGetRefundAddress(ticker: String, validationRegex: Pair<String, String?>) {
        enterAddressDialog(
            context = requireContext(),
            title = getString(R.string.swap_refund_address_title),
            ticker = ticker,
            depositAddressTitle = getString(R.string.swap_refund_address_entry_title, ticker),
            addressValidationRegex = validationRegex.first,
            memoValidationRegex = validationRegex.second,
            setAddress = ::setRefundAddress
        )
    }

    fun enterAddressDialog(
        context: Context,
        title: String,
        ticker: String,
        depositAddressTitle: String,
        addressValidationRegex: String,
        memoValidationRegex: String?,
        setAddress: (address: String, memo: String) -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_swap_deposit_address)
        val sheetTitle = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)!!
        val depositAddrTitle =
            bottomSheetDialog.findViewById<TextView>(R.id.deposit_address_title)!!
        val memoTitle =
            bottomSheetDialog.findViewById<TextView>(R.id.memo_title)!!
        val memo = bottomSheetDialog.findViewById<EditText>(R.id.memo)!!
        val address = bottomSheetDialog.findViewById<EditText>(R.id.address)!!
        val memoContainer = bottomSheetDialog.findViewById<CardView>(R.id.memo_container)!!
        val confirm = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.confirm)!!
        val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
        val validationError = bottomSheetDialog.findViewById<TextView>(R.id.validation_error)!!

        bottomSheetDialog.setCancelable(false)
        sheetTitle.text = title
        depositAddrTitle.text = depositAddressTitle
        memoTitle.isVisible = memoValidationRegex != null
        memoContainer.isVisible = memoValidationRegex != null

        address.doOnTextChanged { text, start, before, count ->
            validationError.isVisible = false
        }

        memo.doOnTextChanged { text, start, before, count ->
            validationError.isVisible = false
        }

        confirm.onClick {
            if(address.text.toString().isEmpty() ||
                !address.text.toString().matches(Regex(addressValidationRegex))) {
                validationError.isVisible = true
                validationError.text = getString(R.string.swap_deposit_address_dialog_invalid_address_error, ticker.lowercase())
            } else {

                if(!NetworkUtil.hasInternet(requireContext())) {
                    validationError.isVisible = true
                    validationError.text = getString(R.string.no_internet_connection)
                } else if(memoValidationRegex != null && (memo.text.toString().isEmpty() ||
                            !memo.text.toString().matches(Regex(memoValidationRegex)))) {
                    validationError.isVisible = true
                    validationError.text = getString(R.string.swap_deposit_address_dialog_invalid_memo_error, ticker.lowercase())
                } else {
                    // good to go check if there is no internet if so just cancel the dialog
                    setAddress(address.text.toString(), memo.text.toString())
                    bottomSheetDialog.cancel()
                    viewModel.confirmExchange()
                }
            }
        }

        bottomSheetDialog.setOnCancelListener {
            binding.confirm.enableButton(true)
        }

        cancel.onClick {
            bottomSheetDialog.cancel()
        }

        bottomSheetDialog.show()
    }

    private fun confirmExchange(
        info: SwapViewModel.ExchangeInfoDisplay
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_confirm_swap)
        val recipient = bottomSheetDialog.findViewById<SettingsItemView>(R.id.recipient)!!
        val sender = bottomSheetDialog.findViewById<SettingsItemView>(R.id.sender)!!
        val refundAddress = bottomSheetDialog.findViewById<SettingsItemView>(R.id.refund_address)!!
        val memo = bottomSheetDialog.findViewById<SettingsItemView>(R.id.memo)!!
        val exchangeType = bottomSheetDialog.findViewById<SettingsItemView>(R.id.exchange_type)!!
        val amountSent = bottomSheetDialog.findViewById<SettingsItemView>(R.id.amount_sent)!!
        val amountReceived = bottomSheetDialog.findViewById<SettingsItemView>(R.id.amount_received)!!
        val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
        val exchange = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.exchange)!!

        recipient.setSubTitleText(info.recipient)
        sender.setSubTitleText(info.sender)
        memo.setSubTitleText(info.memo)
        exchangeType.setSubTitleText(info.exchangeType)
        recipient.setSubTitleText(info.recipient)
        amountSent.setSubTitleText(info.amountSent)
        amountReceived.setSubTitleText(info.amountReceived)
        refundAddress.setSubTitleText(info.refundAddress)

        cancel.onClick {
            bottomSheetDialog.dismiss()
        }

        exchange.onClick {
            // todo impl
        }

        bottomSheetDialog.show()
    }

    override fun onNetworkStateChanged(hasNetwork: Boolean) {
        viewModel.onNoInternet(hasNetwork)
    }


    override fun onNavigateTo(destination: Int) {
        navigate(destination, viewModel.getWalletId())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun actionBarActionRight(): Int {
        return R.drawable.ic_clock
    }

    override fun onActionRight() {

    }

    override fun actionBarSubtitle(): Int {
        return R.string.exchange
    }

    override fun navigationId(): Int {
        return R.id.swapFragment
    }
}
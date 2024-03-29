package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventExchangeCreate
import com.intuisoft.plaid.common.analytics.events.EventExchangeHistoryView
import com.intuisoft.plaid.common.analytics.events.EventExchangeView
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.network.blockchair.response.SupportedCurrencyModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.containsNumbers
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.FragmentExchangeBinding
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.ExchangeViewModel
import com.intuisoft.plaid.features.homescreen.adapters.SupportedCryptoCurrenciesAdapter
import com.intuisoft.plaid.util.NetworkUtil
import com.intuisoft.plaid.util.fragmentconfig.BasicConfigData
import io.horizontalsystems.bitcoinkit.BitcoinKit
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class ExchangeFragment : ConfigurableFragment<FragmentExchangeBinding>(pinProtection = true) {
    private val viewModel: ExchangeViewModel by viewModel()
    private val localStore: LocalStoreRepository by inject()
    protected val eventTracker: EventTracker by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentExchangeBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())
        return binding.root
    }


    override fun onConfiguration(configuration: FragmentConfiguration?) {
        eventTracker.log(EventExchangeView())
        viewModel.setInitialValues()
        binding.swapPairSend.setOnTextChangedListener {
            viewModel.validateSendAmount(
                if(it?.isNotEmpty() == true && it.containsNumbers())
                    it.replace(",", "").toDouble()
                else 0.0
            )
        }

        viewModel.sendPairInfo.observe(viewLifecycleOwner, Observer {
            if(it.ticker.lowercase() == Constants.Strings.BTC_TICKER) {
                binding.swapPairSend.setTickerSymbol(R.drawable.ic_bitcoin)
            } else if(it.symbol != null)
                binding.swapPairSend.setTickerSymbol(it.symbol)
            else binding.swapPairSend.setTickerSymbol(0)

            binding.swapPairSend.setPairTitle(it.pairSendReciveTitle)
            binding.swapPairSend.setStyle(it.pairType)
            binding.swapPairSend.setTicker(it.ticker)
            binding.swapPairSend.setValue(it.receiveValue)
        })

        binding.swapSendReceive.setOnSingleClickListener {
            viewModel.swapSendReceive()
        }

        viewModel.onDisplayExplanation.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), it, true)
        })

        viewModel.estimatedReceiveAmount.observe(viewLifecycleOwner, Observer {
            if(it == null) binding.swapPairReceive.setLoading()
            else binding.swapPairReceive.setValue(it)
        })

        viewModel.disableBtcSeding.observe(viewLifecycleOwner, Observer {
            binding.swapSendReceive.isEnabled = false
            binding.swapSendReceive.alpha = 0.5f
        })

        viewModel.confirmButtonEnabled.observe(viewLifecycleOwner, Observer {
            binding.confirm.enableButton(it)
        })

        binding.confirm.onClick {
            binding.confirm.enableButton(false)
            viewModel.onNext()
        }

        viewModel.recievePairInfo.observe(viewLifecycleOwner, Observer {
            if(it.ticker.lowercase() == Constants.Strings.BTC_TICKER) {
                binding.swapPairReceive.setTickerSymbol(R.drawable.ic_bitcoin)
            } else if(it.symbol != null)
                binding.swapPairReceive.setTickerSymbol(it.symbol)
            else binding.swapPairReceive.setTickerSymbol(0)

            binding.swapPairReceive.setPairTitle(it.pairSendReciveTitle)
            binding.swapPairReceive.setStyle(it.pairType)
            binding.swapPairReceive.setTicker(it.ticker)
            binding.swapPairReceive.setValue(it.receiveValue)
        })

        viewModel.creatingExchange.observe(viewLifecycleOwner, Observer {
            activateAnimatedLoading(it, getString(R.string.swap_create_message))
        })

        viewModel.range.observe(viewLifecycleOwner, Observer {
            setMinMax(it)
        })

        binding.swapPairSend.onTickerClicked {
            supportedCurrenciesDialog(true)
        }

        binding.swapPairReceive.onTickerClicked {
            supportedCurrenciesDialog(false)
        }

        viewModel.screenFunctionsEnabled.observe(viewLifecycleOwner, Observer {
            binding.swapSendReceive.isVisible = it
            binding.loading.isVisible = !it
            binding.swapPairSend.setTickerClickable(it)
            binding.swapPairReceive.setTickerClickable(it)
        })

        viewModel.conversionAmount.observe(viewLifecycleOwner, Observer {
            binding.swapPairReceive.setValue(it)
        })

        viewModel.getReceiveAddress.observe(viewLifecycleOwner, Observer {
            onGetReceiveAddress(it)
        })

        viewModel.getRefundAddress.observe(viewLifecycleOwner, Observer {
            onGetRefundAddress(it)
        })

        viewModel.showContent.observe(viewLifecycleOwner, Observer {
            if(it && viewModel.getWalletNetwork() == BitcoinKit.NetworkType.TestNet) {
                activateContentUnavailable(true, getString(R.string.content_not_available))
            } else {
                activateNoInternet(!it)
            }
        })

        viewModel.exchangeInfoDisplay.observe(viewLifecycleOwner, Observer {
            confirmExchange(it)
        })

        viewModel.onNext.observe(viewLifecycleOwner, Observer {
            eventTracker.log(EventExchangeCreate())
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    configurationType = FragmentConfigurationType.CONFIGURATION_SWAP_DATA,
                    configData = BasicConfigData(
                        payload = Gson().toJson(it, ExchangeInfoDataModel::class.java)
                    )
                )
            )

            navigate(
                R.id.exchangeDetailsFragment,
                bundle,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        })
    }

    override fun onBackPressed() {
        onNavigateBottomBarSecondaryFragmentBackwards()
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

    private fun checkAddress(address: String, ticker: SupportedCurrencyModel, onResult: (Pair<Boolean, String?>) -> Unit) {
        viewModel.checkAddress(ticker, address, onResult)
    }

    private fun onGetReceiveAddress(currency: SupportedCurrencyModel) {
        enterAddressDialog(
            context = requireContext(),
            title = getString(R.string.swap_deposit_address_title),
            currency = currency,
            depositAddressTitle = getString(R.string.swap_deposit_address_entry_title, currency.ticker),
            setAddress = ::setReceiveAddress,
            checkAddress = ::checkAddress
        )
    }

    private fun onGetRefundAddress(currency: SupportedCurrencyModel) {
        enterAddressDialog(
            context = requireContext(),
            title = getString(R.string.swap_refund_address_title),
            currency = currency,
            depositAddressTitle = getString(R.string.swap_refund_address_entry_title, currency.ticker),
            setAddress = ::setRefundAddress,
            checkAddress = ::checkAddress
        )
    }

    fun enterAddressDialog(
        context: Context,
        title: String,
        currency: SupportedCurrencyModel,
        depositAddressTitle: String,
        setAddress: (address: String, memo: String) -> Unit,
        checkAddress: (address: String, ticker: SupportedCurrencyModel, onResult: (Pair<Boolean, String?>) -> Unit) -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)
        addToStack(bottomSheetDialog)
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
        memoTitle.isVisible = currency.needsMemo
        memoContainer.isVisible = currency.needsMemo

        address.doOnTextChanged { text, start, before, count ->
            validationError.isVisible = false
        }

        memo.doOnTextChanged { text, start, before, count ->
            validationError.isVisible = false
        }

        confirm.onClick {
            if(address.text.toString().isEmpty()) {
                validationError.isVisible = true
                validationError.text = getString(R.string.swap_deposit_address_dialog_invalid_address_error, currency.ticker.lowercase())
            } else {
                checkAddress(address.text.toString(), currency) { (isValid, error) ->
                    if(!isValid) {
                        validationError.isVisible = true
                        validationError.text = error ?: getString(R.string.swap_deposit_address_dialog_invalid_address_error, currency.ticker.lowercase())
                    } else if (!NetworkUtil.hasInternet(requireContext())) {
                        validationError.isVisible = true
                        validationError.text = getString(R.string.no_internet_connection)
                    } else if (currency.needsMemo && memo.text.toString().isEmpty()) {
                        validationError.isVisible = true
                        validationError.text = getString(
                            R.string.swap_deposit_address_dialog_invalid_memo_error,
                            currency.ticker.lowercase()
                        )
                    } else {
                        // good to go check if there is no internet if so just cancel the dialog
                        setAddress(address.text.toString(), memo.text.toString())
                        bottomSheetDialog.cancel()
                        viewModel.confirmExchange()
                    }
                }
            }
        }

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
            _binding?.confirm?.enableButton(true)
        }

        cancel.onClick {
            bottomSheetDialog.cancel()
        }

        bottomSheetDialog.show()
    }

    fun supportedCurrenciesDialog(sending: Boolean) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        addToStack(bottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_supported_currencies)
        val searchResults = bottomSheetDialog.findViewById<RecyclerView>(R.id.searchResults)!!
        val search = bottomSheetDialog.findViewById<EditText>(R.id.search)!!

        val adapter = SupportedCryptoCurrenciesAdapter(
            onCurrencySelected = {
                bottomSheetDialog.cancel()
                binding.swapPairSend.setValue(0.0)
                if(sending) {
                    viewModel.setOutboundCurrency(it)
                }
                else viewModel.setInboundCurrency(it)
            }
        )

        val onResult: (Pair<List<SupportedCurrencyModel>, List<SupportedCurrencyModel>>) -> Unit = { (mostUsed, results) ->
            adapter.addCurrencies(requireContext(), mostUsed.toArrayList(), results.toArrayList())
        }

        searchResults.adapter = adapter
        viewModel.updateSearchValue("", onResult)

        search.doOnTextChanged { text, start, before, count ->
            viewModel.updateSearchValue(text?.toString() ?: "", onResult)
        }

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
        }
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.behavior.isFitToContents = false
        bottomSheetDialog.show()
    }

    private fun confirmExchange(
        info: ExchangeViewModel.ExchangeInfoDisplay
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        addToStack(bottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_confirm_swap)
        val recipient = bottomSheetDialog.findViewById<SettingsItemView>(R.id.recipient)!!
        val sender = bottomSheetDialog.findViewById<SettingsItemView>(R.id.sender)!!
        val refundAddress = bottomSheetDialog.findViewById<SettingsItemView>(R.id.refund_address)!!
        val memo = bottomSheetDialog.findViewById<SettingsItemView>(R.id.memo)!!
        val amountSent = bottomSheetDialog.findViewById<SettingsItemView>(R.id.amount_sent)!!
        val amountReceived = bottomSheetDialog.findViewById<SettingsItemView>(R.id.amount_received)!!
        val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
        val exchange = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.exchange)!!

        recipient.setSubTitleText(info.recipient)
        sender.setSubTitleText(info.sender)
        memo.setSubTitleText(info.memo)
        recipient.setSubTitleText(info.recipient)
        amountSent.setSubTitleText(info.amountSent)
        amountReceived.setSubTitleText(info.amountReceived)
        refundAddress.setSubTitleText(info.refundAddress)

        cancel.onClick {
            bottomSheetDialog.dismiss()
        }

        exchange.onClick {
            bottomSheetDialog.dismiss()
            viewModel.createExchange()
        }

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
        }
        bottomSheetDialog.show()
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

    override fun actionBarActionRight(): Int {
        if(viewModel.getWalletNetwork() == BitcoinKit.NetworkType.TestNet) {
            return 0
        } else return R.drawable.ic_clock
    }

    override fun onActionRight() {
        eventTracker.log(EventExchangeHistoryView())
        navigate(
            R.id.exchangeHistoryFragment,
            Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
        )
    }

    override fun actionBarSubtitle(): Int {
        return R.string.exchange
    }

    override fun navigationId(): Int {
        return R.id.exchangeFragment
    }
}
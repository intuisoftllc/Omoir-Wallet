package com.intuisoft.plaid.features.dashboardscreen.ui

import android.Manifest
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.docformative.docformative.toArrayList
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentWithdrawBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.CurrencyViewModel
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WithdrawalViewModel
import com.intuisoft.plaid.features.homescreen.adapters.CoinControlAdapter
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.listeners.BarcodeResultListener
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.model.FeeType
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.*
import io.horizontalsystems.bitcoincore.extensions.toHexString
import io.horizontalsystems.bitcoincore.serializers.TransactionSerializer
import kotlinx.android.synthetic.main.advanced_options_send.*
import kotlinx.android.synthetic.main.passcode_view.view.*
import kotlinx.android.synthetic.main.settings_item.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class WithdrawalFragment : PinProtectedFragment<FragmentWithdrawBinding>(), BarcodeResultListener {
    private val viewModel: WithdrawalViewModel by viewModel()
    private val currencyViewModel: CurrencyViewModel by viewModel()
    private val localStoreRepository: LocalStoreRepository by inject()
    private var overBalanceFailures = 5

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWithdrawBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf()
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {

        viewModel.showWalletDisplayUnit()
        viewModel.setInitialFeeRate()
        viewModel.setLocalSpendAmount(0.0, viewModel.getCurrentRateConversion())

        binding.close.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.amount.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //here is your code
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                if(s.isNotEmpty() && !viewModel.validateSendAmount(s.toString())) {
                    binding.amount.setText(s.toString().dropLast(1))
                    binding.amount.setSelection(binding.amount.text.length)
                    onAmountOverBalanceAnimation()

                    overBalanceFailures++
                    if(overBalanceFailures % 5 == 0 && overBalanceFailures > 1) {
                        styledSnackBar(requireView(), "You cannot enter a higher balance than you have, try changing the denomination.", true)
                    }
                } else if(s.isEmpty()){
                    viewModel.setLocalSpendAmount(0.0, viewModel.getCurrentRateConversion())
                }
            }
        })

        binding.address.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //here is your code
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {
                viewModel.setLocalAddress(s.toString())
            }
        })

        viewModel.walletBalance.observe(viewLifecycleOwner, Observer {
            binding.balance.text = it
        })

        viewModel.onNotEnoughFunds.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), "Amount + Fee is too high based in the available inputs.", true)
        })

        viewModel.somethingWentWrong.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), "Oops something went wrong.", true)
        })

        viewModel.walletDisplayUnit.observe(viewLifecycleOwner, Observer {

            if(viewModel.swapCurrencyAndAmount()) {
                binding.displayUnit.setBackgroundResource(R.drawable.ic_currency_euro)
            } else {
                when (it) {
                    BitcoinDisplayUnit.BTC -> {
                        binding.displayUnit.setBackgroundResource(R.drawable.ic_bitcoin)
                    }

                    BitcoinDisplayUnit.SATS -> {
                        binding.displayUnit.setBackgroundResource(R.drawable.ic_satoshi)
                    }
                }
            }

            viewModel.showWalletBalance()
        })

        binding.scanButton.onClick {

            requireActivity().checkAppPermission(Manifest.permission.CAMERA, 100) {
                (requireActivity() as MainActivity).scanBarcode()
            }
        }

        binding.displayUnit.setOnClickListener {
            val localAmount = viewModel.getLocalRate().getRawRate()

            when(viewModel.getDisplayUnit()) {
                BitcoinDisplayUnit.BTC -> {
                    viewModel.setDisplayUnit(BitcoinDisplayUnit.SATS)

                    if(localAmount != 0L) {
                        binding.amount.setText(viewModel.getLocalRate().from(RateConverter.RateType.SATOSHI_RATE).first)
                        binding.amount.setSelection(binding.amount.text.length)
                    }
                }

                BitcoinDisplayUnit.SATS -> {
                    viewModel.setCurrencyAmountSwap(!viewModel.swapCurrencyAndAmount())
                    if(viewModel.swapCurrencyAndAmount()) {
                        binding.amount.setText(viewModel.getLocalRate().from(RateConverter.RateType.FIAT_RATE).first)
                        binding.amount.setSelection(binding.amount.text.length)
                    } else {
                        viewModel.setDisplayUnit(BitcoinDisplayUnit.BTC)

                        if (localAmount != 0L) {
                            binding.amount.setText(viewModel.getLocalRate().from(RateConverter.RateType.BTC_RATE).first)
                            binding.amount.setSelection(binding.amount.text.length)
                        }
                    }
                }
            }

            viewModel.showWalletDisplayUnit()
        }

        viewModel.onLocalAmountUpdated.observe(viewLifecycleOwner, Observer {
            binding.currencyAmount.isVisible = it > 0

            if(it > 0) {
                if(viewModel.swapCurrencyAndAmount()){
                    binding.currencyAmount.text = "${viewModel.getLocalRate().from(RateConverter.RateType.SATOSHI_RATE).second}"
                } else {
                    binding.currencyAmount.text = "${viewModel.getLocalRate().from(RateConverter.RateType.FIAT_RATE).second}"
                }
            }

            val fee = viewModel.getTotalFee()
            binding.transactionFee.isVisible = fee > 0

            if(fee > 0) {
                binding.transactionFee.text = "Fee: ${SimpleCoinNumberFormat.format(localStoreRepository, fee, true)}"
            }
        })

        viewModel.onLowPaymetAmount.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), "Payment Amount too low", true)
        })

        viewModel.onTransactionCreationFailed.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), "Failed to create Transaction", true)
        })

        viewModel.onConfirm.observe(viewLifecycleOwner, Observer {
            showConfirmTransactionDialog(false)
        })

        viewModel.onInvalidAddress.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), "Invalid Address", true)
        })

        viewModel.notEnoughPeers.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), "Reconnecting to bitcoin core, please wait...", Toast.LENGTH_LONG).show()
        })

        viewModel.onSpendFullBalance.observe(viewLifecycleOwner, Observer {
            showConfirmTransactionDialog(true)
        })

        viewModel.onTransactionSent.observe(viewLifecycleOwner, Observer {
            // todo impl
        })

        binding.confirm.onClick {
            viewModel.nextStep()
        }

        binding.advancedOptions.onClick {
            showAdvancedOptionsDialog()
        }
    }

    override fun onAddressReceived(address: String) {
        binding.address.setText(address)
    }

    private fun showConfirmTransactionDialog(fullSpend: Boolean) {
        if(fullSpend) {
            viewModel.adjustLocalSpendToFitFee()
        }

        viewModel.createTransaction()?.let { transaction ->

            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(R.layout.confirm_transaction)
            val rawTransaction = bottomSheetDialog.findViewById<SettingsItemView>(R.id.rawTransaction)!!
            val txId = bottomSheetDialog.findViewById<SettingsItemView>(R.id.transactionId)!!
            val to = bottomSheetDialog.findViewById<SettingsItemView>(R.id.sendingTo)!!
            val amount = bottomSheetDialog.findViewById<SettingsItemView>(R.id.txAmount)!!
            val feeAmount = bottomSheetDialog.findViewById<SettingsItemView>(R.id.feeAmount)!!
            val satPerByte = bottomSheetDialog.findViewById<SettingsItemView>(R.id.feeRate)!!
            val fullBalaceNotice = bottomSheetDialog.findViewById<TextView>(R.id.fullBalaceNotice)!!
            val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
            val broadcastTransaction = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.broadcastTransaction)!!
            fullBalaceNotice.isVisible = fullSpend

            val rawTx = TransactionSerializer.serialize(transaction).toHexString()
            rawTransaction.setSubTitleText(rawTx)
            txId.setSubTitleText(transaction.header.hash.toHexString())
            to.setSubTitleText(viewModel.getLocalAddress() ?: "")

            var amountValue = SimpleCoinNumberFormat.format(localStoreRepository, viewModel.getLocalRate().getRawRate(), true)
            if(fullSpend) {
                amountValue += " (adjusted to fit fee)"
            }

            amount.setSubTitleText(amountValue)
            feeAmount.setSubTitleText(SimpleCoinNumberFormat.format(localStoreRepository, viewModel.getTotalFee(), true))
            satPerByte.setSubTitleText("${Plural.of("sat", viewModel.getFeeRate().toLong())}/vbyte")

            rawTransaction.onClick {
                requireContext().copyToClipboard(rawTx, "Raw Transaction")
                rawTransaction.showCopy(false)
                rawTransaction.showCheck(true)

                viewModel.viewModelScope.launch {
                    delay(550)
                    rawTransaction.showCopy(true)
                    rawTransaction.showCheck(false)
                }
            }

            cancel.onClick {
                bottomSheetDialog.dismiss()
            }

            broadcastTransaction.onClick {
                if(viewModel.broadcast(requireContext(), transaction)) {
                    bottomSheetDialog.dismiss()
                }
            }

            bottomSheetDialog.behavior.state = STATE_EXPANDED
            bottomSheetDialog.show()
        }
    }

    private fun showAdvancedOptionsDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(com.intuisoft.plaid.R.layout.advanced_options_send)
        val low = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.lowFee)!!
        val med = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.mediumFee)!!
        val high = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.highFee)!!
        val feePerByte = bottomSheetDialog.findViewById<EditText>(R.id.feePerByte)!!
        val coinControl = bottomSheetDialog.findViewById<SettingsItemView>(R.id.coinControlOption)!!

        val utxos = viewModel.getUnspentOutputs()
        setSelectedFeeType(low, med, high, localStoreRepository.getDefaultFeeType())
        val feeRate = viewModel.getNetworkFeeRate()
        feePerByte.setText("${viewModel.getFeeRate()}")
        coinControl.setSubTitleText(Plural.of("Address", utxos.size.toLong(), "es"))

        coinControl.onClick {
            bottomSheetDialog.cancel()
            showCoinControlBottomSheet()
        }

        low.onClick {
            setFeeRate(FeeType.LOW, feeRate.lowFee, feePerByte, low, med, high)
        }

        med.onClick {
            setFeeRate(FeeType.MED, feeRate.medFee, feePerByte, low, med, high)
        }

        high.onClick {
            setFeeRate(FeeType.HIGH, feeRate.highFee, feePerByte, low, med, high)
        }

        feePerByte.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //here is your code
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // TODO Auto-generated method stub
            }

            override fun afterTextChanged(s: Editable) {

                if(s.isNotEmpty() && s.toString().toInt() > 0) {
                    val rate = s.toString().toInt()

                    when {
                        rate== 0 -> {
                            feePerByte.setText("1")
                        }
                        (feeRate.lowFee..feeRate.medFee).contains(rate) -> {
                            setSelectedFeeType(low, med, high, FeeType.LOW)
                        }

                        (feeRate.medFee..feeRate.highFee).contains(rate) -> {
                            setSelectedFeeType(low, med, high, FeeType.MED)
                        }

                        rate >= feeRate.highFee -> {
                            setSelectedFeeType(low, med, high, FeeType.HIGH)
                        }
                    }

                    viewModel.setFeeRate(rate)
                } else {
                    viewModel.setFeeRate(1)
                }
            }
        })

        bottomSheetDialog.show()
    }

    fun showCoinControlBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(com.intuisoft.plaid.R.layout.advanced_options_coin_control)
        val selectAll = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.selectAllButton)!!
        val noUTXOs = bottomSheetDialog.findViewById<TextView>(R.id.noUTXOAvailable)!!
        val unspentOutputsList = bottomSheetDialog.findViewById<RecyclerView>(R.id.unspentOutputs)!!
        val utxos = viewModel.getUnspentOutputs()

        if(utxos.isEmpty()) {
            selectAll.enableButton(false)
            noUTXOs.isVisible = true
            unspentOutputsList.isVisible = false
        } else {
            val adapter = CoinControlAdapter(localStoreRepository) {
                if(it) {
                    selectAll.setButtonStyle(RoundedButtonView.ButtonStyle.OUTLINED_STYLE)
                } else {
                    selectAll.setButtonStyle(RoundedButtonView.ButtonStyle.WHITE_ROUNDED_STYLE)
                }
            }

            unspentOutputsList.adapter = adapter
            adapter.addUTXOs(utxos.toArrayList(), viewModel.getSelectedUTXOs().toArrayList())

            selectAll.onClick {
                adapter.selectAll(!adapter.areAllItemsSelected())
            }

            bottomSheetDialog.setOnCancelListener {
                viewModel.updateUTXOs(adapter.selectedUTXOs)
            }
        }

        bottomSheetDialog.show()
    }

    fun setFeeRate(
        feeType: FeeType,
        rate: Int,
        feePerByte: EditText,
        low: RoundedButtonView,
        med: RoundedButtonView,
        high: RoundedButtonView) {

        when(feeType) {
            FeeType.LOW -> {
                feePerByte.setText("${rate}")
                setSelectedFeeType(low, med, high, FeeType.LOW)
            }

            FeeType.MED -> {
                feePerByte.setText("${rate}")
                setSelectedFeeType(low, med, high, FeeType.MED)
            }

            FeeType.HIGH -> {
                feePerByte.setText("${rate}")
                setSelectedFeeType(low, med, high, FeeType.HIGH)
            }
        }
    }

    fun setSelectedFeeType(low: RoundedButtonView, med: RoundedButtonView, high: RoundedButtonView, type: FeeType) {
        when(type) {
            FeeType.LOW -> {
                low.setButtonStyle(RoundedButtonView.ButtonStyle.OUTLINED_STYLE)
                med.setButtonStyle(RoundedButtonView.ButtonStyle.WHITE_ROUNDED_STYLE)
                high.setButtonStyle(RoundedButtonView.ButtonStyle.WHITE_ROUNDED_STYLE)
                localStoreRepository.setDefaultFeeType(FeeType.LOW)
            }

            FeeType.MED -> {
                low.setButtonStyle(RoundedButtonView.ButtonStyle.WHITE_ROUNDED_STYLE)
                med.setButtonStyle(RoundedButtonView.ButtonStyle.OUTLINED_STYLE)
                high.setButtonStyle(RoundedButtonView.ButtonStyle.WHITE_ROUNDED_STYLE)
                localStoreRepository.setDefaultFeeType(FeeType.MED)
            }

            FeeType.HIGH -> {
                low.setButtonStyle(RoundedButtonView.ButtonStyle.WHITE_ROUNDED_STYLE)
                med.setButtonStyle(RoundedButtonView.ButtonStyle.WHITE_ROUNDED_STYLE)
                high.setButtonStyle(RoundedButtonView.ButtonStyle.OUTLINED_STYLE)
                localStoreRepository.setDefaultFeeType(FeeType.HIGH)
            }
        }
    }


    fun onAmountOverBalanceAnimation() {
        val shake: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.shake)
        binding.amount.startAnimation(shake)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showActionBar(): Boolean {
        if(configSet())
            return super.showActionBar()

        return false
    }

    override fun actionBarTitle(): Int {
        if(configSet())
            return super.actionBarTitle()

        return 0
    }

    override fun navigationId(): Int {
        return R.id.withdrawalFragment
    }
}
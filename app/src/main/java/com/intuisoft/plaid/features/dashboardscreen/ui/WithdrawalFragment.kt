package com.intuisoft.plaid.features.dashboardscreen.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentWithdrawBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.CurrencyViewModel
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WithdrawalViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.model.FeeType
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.RateConverter
import com.intuisoft.plaid.util.SimpleCoinNumberFormat
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class WithdrawalFragment : PinProtectedFragment<FragmentWithdrawBinding>() {
    private val viewModel: WithdrawalViewModel by sharedViewModel()
    private val currencyViewModel: CurrencyViewModel by viewModel()
    private val localStoreRepository: LocalStoreRepository by inject()

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

        binding.displayUnit.setOnClickListener {
            val localAmount = viewModel.getLocalRate().getRawRate()

            when(viewModel.getDisplayUnit()) {
                BitcoinDisplayUnit.BTC -> {
                    viewModel.setDisplayUnit(BitcoinDisplayUnit.SATS)

                    if(localAmount != 0L) {
                        binding.amount.setText(viewModel.getLocalRate().from(RateConverter.RateType.SATOSHI_RATE).first)
                    }
                }

                BitcoinDisplayUnit.SATS -> {
                    viewModel.setCurrencyAmountSwap(!viewModel.swapCurrencyAndAmount())
                    if(viewModel.swapCurrencyAndAmount()) {
                        binding.amount.setText(viewModel.getLocalRate().from(RateConverter.RateType.FIAT_RATE).first)
                    } else {
                        viewModel.setDisplayUnit(BitcoinDisplayUnit.BTC)

                        if (localAmount != 0L) {
                            binding.amount.setText(viewModel.getLocalRate().from(RateConverter.RateType.BTC_RATE).first)
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

            val fee = viewModel.calculateFee(it, viewModel.getFeeRate(), null)
            binding.transactionFee.isVisible = fee > 0

            if(fee > 0) {
                binding.transactionFee.text = "Fee: ${SimpleCoinNumberFormat.format(localStoreRepository, fee, true)}"
            }
        })

        viewModel.onLowPaymetAmount.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), "Payment Amount too low", true)
        })

        viewModel.onNextStep.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), "Next Step!", true)
        })

        viewModel.onInvalidAddress.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), "Invalid Address", true)
        })

        viewModel.onSpendFullBalance.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), "Ful Spend", true)
        })

        binding.nextButton.onClick {
            viewModel.nextStep()
        }

        binding.advancedOptions.onClick {
            showAdvancedOptionsDialog()
        }
    }


    private fun showAdvancedOptionsDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(com.intuisoft.plaid.R.layout.advanced_options_send)
        val low = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.lowFee)!!
        val med = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.mediumFee)!!
        val high = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.highFee)!!
        val feePerByte = bottomSheetDialog.findViewById<EditText>(R.id.feePerByte)!!

        setSelectedFeeType(low, med, high, localStoreRepository.getDefaultFeeType())
        val feeRate = viewModel.getNetworkFeeRate()
        feePerByte.setText("${viewModel.getFeeRate()}")

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
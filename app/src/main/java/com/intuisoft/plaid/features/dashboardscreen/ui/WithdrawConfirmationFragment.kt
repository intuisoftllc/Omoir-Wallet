package com.intuisoft.plaid.features.dashboardscreen.ui

import android.Manifest
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentWithdrawConfirmationBinding
import com.intuisoft.plaid.features.dashboardscreen.adapters.AddressBookAdapter
import com.intuisoft.plaid.features.dashboardscreen.adapters.TransferToWalletAdapter
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WithdrawConfirmationViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.features.settings.ui.AddressBookFragment
import com.intuisoft.plaid.features.settings.viewmodel.AddressBookViewModel
import com.intuisoft.plaid.listeners.BarcodeResultListener
import com.intuisoft.plaid.common.model.FeeType
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Plural
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.util.fragmentconfig.SendFundsData
import io.horizontalsystems.bitcoincore.extensions.toHexString
import io.horizontalsystems.bitcoincore.serializers.TransactionSerializer
import kotlinx.android.synthetic.main.fragment_wallet_export.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class WithdrawConfirmationFragment : PinProtectedFragment<FragmentWithdrawConfirmationBinding>(), BarcodeResultListener {
    private val viewModel: WithdrawConfirmationViewModel by viewModel()
    private val addressBookVM: AddressBookViewModel by viewModel()
    private val localStoreRepository: LocalStoreRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWithdrawConfirmationBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_WITHDRAW
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        val data = configuration!!.configData as SendFundsData

        viewModel.updateUTXOs(data.spendFrom.toMutableList())
        viewModel.updateSendAmount(data.amountToSend)
        viewModel.setNetworkFeeRate()

        binding.scan.onClick {
            requireActivity().checkAppPermission(Manifest.permission.CAMERA, 100) {
                (requireActivity() as MainActivity).scanBarcode()
            }
        }

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

        binding.advancedOptions.setOnClickListener {
            showAdvancedOptionsDialog()
        }


        viewModel.onInputRejected.observe(viewLifecycleOwner, Observer {
            onInvalidBitcoinAddressAnimation()
        })

        viewModel.onDisplayExplanation.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), it, true)
        })

        viewModel.onNetworkError.observe(viewLifecycleOwner, Observer {
            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        })

        binding.confirm.onClick {
            viewModel.nextStep()
        }

        viewModel.onSpendFullBalance.observe(viewLifecycleOwner, Observer {
            showConfirmTransactionDialog(true)
        })

        viewModel.onConfirm.observe(viewLifecycleOwner, Observer {
            showConfirmTransactionDialog(false)
        })
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
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_confirm_withdrawl)
            val rawTransaction = bottomSheetDialog.findViewById<SettingsItemView>(R.id.raw_transaction)!!
            val txId = bottomSheetDialog.findViewById<SettingsItemView>(R.id.transaction_id)!!
            val to = bottomSheetDialog.findViewById<SettingsItemView>(R.id.sending_to)!!
            val amount = bottomSheetDialog.findViewById<SettingsItemView>(R.id.tx_amount)!!
            val feeAmount = bottomSheetDialog.findViewById<SettingsItemView>(R.id.fee_amount)!!
            val satPerByte = bottomSheetDialog.findViewById<SettingsItemView>(R.id.fee_rate)!!
            val fullBalaceNotice = bottomSheetDialog.findViewById<TextView>(R.id.fullBalaceNotice)!!
            val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
            val broadcastTransaction = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.broadcastTransaction)!!
            fullBalaceNotice.isVisible = fullSpend

            val rawTx = TransactionSerializer.serialize(transaction).toHexString()
            val feePaid = viewModel.getTotalFee()
            rawTransaction.setSubTitleText(rawTx)
            txId.setSubTitleText(transaction.header.hash.toHexString())
            to.setSubTitleText(viewModel.getLocalAddress() ?: "")

            var amountValue = viewModel.getSpendAmount().from(viewModel.getDisplayUnit().toRateType(), false).second
            if(fullSpend) {
                amountValue += " ${getString(R.string.withdraw_confirmation_dialog_adjusted_balance)}"
            }

            amount.setSubTitleText(amountValue)
            feeAmount.setSubTitleText(SimpleCoinNumberFormat.format(localStoreRepository, feePaid, false))
            satPerByte.setSubTitleText("${Plural.of("sat", viewModel.getFeeRate().toLong())}/vbyte")

            rawTransaction.onClick {
                requireContext().copyToClipboard(rawTx, getString(R.string.raw_transaction))
                rawTransaction.showCopy(false)
                rawTransaction.showCheck(true)

                viewModel.viewModelScope.launch {
                    delay(550)
                    rawTransaction.showCopy(true)
                    rawTransaction.showCheck(false)
                }
            }

            txId.onClick {
                requireContext().copyToClipboard(transaction.header.hash.toHexString(), getString(R.string.transaction_id))
                txId.showCopy(false)
                txId.showCheck(true)

                viewModel.viewModelScope.launch {
                    delay(550)
                    txId.showCopy(true)
                    txId.showCheck(false)
                }
            }

            cancel.onClick {
                bottomSheetDialog.dismiss()
            }

            broadcastTransaction.onClick {
                if(viewModel.isFingerprintEnabled()) {
                    validateFingerprint(
                        title = com.intuisoft.plaid.common.util.Constants.Strings.USE_BIOMETRIC_AUTH,
                        subTitle = com.intuisoft.plaid.common.util.Constants.Strings.USE_BIOMETRIC_REASON_6,
                        onSuccess = {
                            if(viewModel.broadcast(transaction)) {
                                bottomSheetDialog.dismiss()
                                showAlertDialogButtonClicked(feePaid)
                            }
                        }
                    )
                } else {
                    if(viewModel.broadcast(transaction)) {
                        bottomSheetDialog.dismiss()
                        showAlertDialogButtonClicked(feePaid)
                    }
                }
            }

            bottomSheetDialog.behavior.state = STATE_EXPANDED
            bottomSheetDialog.show()
        }
    }

    fun showAlertDialogButtonClicked(feePaid: Long) {
        val dialog = Dialog(requireActivity())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_transaction_sent)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val fee = dialog.findViewById<TextView>(R.id.fee)
        val spendAmount = dialog.findViewById<TextView>(R.id.spend_amount)
        val done = dialog.findViewById<RoundedButtonView>(R.id.done)
        val successIcon = dialog.findViewById<ImageView>(R.id.success_icon)

        successIcon.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800)
        fee.text = getString(R.string.withdraw_confirmation_dialog_success_fee, SimpleCoinNumberFormat.format(localStoreRepository, feePaid, true))

        viewModel.onAnimateSentAmount.observe(viewLifecycleOwner, Observer {
            spendAmount.text = it
        })

        viewModel.animateSentAmount(800)
        done.onClick {
            dialog.cancel()
            findNavController().popBackStack(R.id.walletDashboardFragment, false)
        }

        dialog.show()
    }

    fun showAddressBookBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_address_book)
        val addAddress = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.add_address)!!
        val noAddresses = bottomSheetDialog.findViewById<TextView>(R.id.no_addresses)!!
        val addresses = bottomSheetDialog.findViewById<RecyclerView>(R.id.addresses)!!
        val savedAddresses = localStoreRepository.getSavedAddresses()

        if(savedAddresses.isEmpty()) {
            noAddresses.isVisible = true
            addresses.isVisible = false
        } else {
            val adapter = AddressBookAdapter {
                binding.address.setText(it)
                bottomSheetDialog.cancel()
            }

            addresses.adapter = adapter
            adapter.addSavedAddresses(savedAddresses.toArrayList())
        }


        addAddress.onClick {
            bottomSheetDialog.cancel()
            AddressBookFragment.showSaveAddressDialog(
                context = requireContext(),
                titleText = getString(R.string.save_address_title),
                saveButtonText = getString(R.string.save),
                cancelButtonText = getString(R.string.cancel),
                isAddressValid = {
                    viewModel.isAddressValid(it)
                },
                saveAddress = { name, address ->
                    if (addressBookVM.savedAddressExists(name)) {
                        false
                    } else {
                        addressBookVM.saveAddress(name, address)
                        showAddressBookBottomSheet()
                        true
                    }
                }
            )
        }

        bottomSheetDialog.show()
    }

    fun showSendToWalletBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_wallet_transfer)
        val noWallets = bottomSheetDialog.findViewById<TextView>(R.id.no_wallets)!!
        val wallets = bottomSheetDialog.findViewById<RecyclerView>(R.id.wallets)!!
        val walletsList = viewModel.getWallets().filter {
            viewModel.canTransferToWallet(it)
        }

        if(walletsList.isEmpty()) {
            noWallets.isVisible = true
            wallets.isVisible = false
        } else {
            val adapter = TransferToWalletAdapter(localStoreRepository) {
                binding.address.setText(it.walletKit!!.receiveAddress())
                styledSnackBar(requireView(), getString(R.string.withdraw_confirmation_transfer_notice, it.name), true)
                bottomSheetDialog.cancel()
            }

            wallets.adapter = adapter
            adapter.addWallets(walletsList.toArrayList())
        }

        bottomSheetDialog.show()
    }

    private fun showAdvancedOptionsDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_withdraw_advanced_options)
        val low = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.slow)!!
        val med = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.med)!!
        val high = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.fast)!!
        val feePerByte = bottomSheetDialog.findViewById<EditText>(R.id.fee_rate)!!
        val addressBook = bottomSheetDialog.findViewById<SettingsItemView>(R.id.address_book)!!
        val sendToOtherWallets = bottomSheetDialog.findViewById<SettingsItemView>(R.id.transfer_to_wallet)!!
        val feeRate = viewModel.getNetworkFeeRate()

        addressBook.onClick {
            bottomSheetDialog.cancel()
            showAddressBookBottomSheet()
        }

        sendToOtherWallets.onClick {
            bottomSheetDialog.cancel()
            showSendToWalletBottomSheet()
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
                        (feeRate.lowFee until feeRate.medFee).contains(rate) -> {
                            setSelectedFeeType(low, med, high, FeeType.LOW, false)
                        }

                        (feeRate.medFee until feeRate.highFee).contains(rate) -> {
                            setSelectedFeeType(low, med, high, FeeType.MED, false)
                        }

                        rate >= feeRate.highFee -> {
                            setSelectedFeeType(low, med, high, FeeType.HIGH, false)
                        }
                    }

                    viewModel.setFeeRate(rate)
                } else {
                    viewModel.setFeeRate(1)
                }
            }
        })

        feePerByte.setText("${viewModel.getFeeRate()}")
        addressBook.setSubTitleText(Plural.of("Address", localStoreRepository.getSavedAddresses().size.toLong(), "es"))

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

    fun setSelectedFeeType(low: RoundedButtonView, med: RoundedButtonView, high: RoundedButtonView, type: FeeType, save: Boolean = true) {
        when(type) {
            FeeType.LOW -> {
                low.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                med.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                high.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                if(save) localStoreRepository.setDefaultFeeType(FeeType.LOW)
            }

            FeeType.MED -> {
                low.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                med.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                high.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                if(save) localStoreRepository.setDefaultFeeType(FeeType.MED)
            }

            FeeType.HIGH -> {
                low.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                med.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                high.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                if(save) localStoreRepository.setDefaultFeeType(FeeType.HIGH)
            }
        }
    }


    fun onInvalidBitcoinAddressAnimation() {
        val shake: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.shake)
        binding.address.startAnimation(shake)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun navigationId(): Int {
        return R.id.withdrawConfirmtionFragment
    }
}
package com.intuisoft.plaid.features.dashboardflow.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.databinding.FragmentExchangeDetailsBinding
import com.intuisoft.plaid.features.dashboardflow.viewmodel.SwapDetailsViewModel
import com.intuisoft.plaid.model.ExchangeStatus
import com.intuisoft.plaid.util.fragmentconfig.ConfigInvoiceData
import com.intuisoft.plaid.util.fragmentconfig.ConfigSwapData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExchangeDetailsFragment : PinProtectedFragment<FragmentExchangeDetailsBinding>() {
    protected val viewModel: SwapDetailsViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentExchangeDetailsBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_SWAP_DATA
            )
        )
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onConfiguration(configuration: FragmentConfiguration?) {
        val swapData = configuration!!.configData as ConfigSwapData
        val data = Gson().fromJson(swapData.payload, ExchangeInfoDataModel::class.java)

        binding.status.text = data.status
        binding.status.setTextColor(
            resources.getColor(
            ExchangeStatus.values().find { it.type == data.status }?.color ?: R.color.text_grey
            )
        )
        binding.from.text = data.from
        binding.to.text = data.to
        binding.sendAmount.text = "${SimpleCoinNumberFormat.formatCrypto(data.sendAmount)} ${data.fromShort}"
        binding.receiveAmount.text = "${SimpleCoinNumberFormat.formatCrypto(data.receiveAmount)} ${data.toShort}"
        binding.fiatConversionContainer.isVisible = data.toShort.lowercase() == Constants.Strings.BTC_TICKER
        binding.sendAmount2.text = "${SimpleCoinNumberFormat.formatCrypto(data.sendAmount)} ${data.fromShort}"
        binding.paymentAddress.text = data.paymentAddress
        binding.paymentAddressContainer.setOnClickListener {
            viewModel.copyDataItemClicked(
                binding.copyPaymentAddress, data.paymentAddress
            )
        }
        binding.viewFullDetails.setOnClickListener {
            openLink(getString(R.string.swap_view_full_details_link, data.id))
        }
        binding.transactionIdContainer.setOnClickListener {
            if(data.paymentTxId?.isNotEmpty() == true) {
                viewModel.copyDataItemClicked(
                    binding.copyTransactionId, data.paymentTxId!!
                )
            }
        }
        binding.sendAmountContainer.setOnClickListener {
            viewModel.copyDataItemClicked(
                binding.copyPaymentAmount, SimpleCoinNumberFormat.formatCrypto(data.sendAmount) ?: ""
            )
        }

        viewModel.copyData.observe(viewLifecycleOwner, Observer { (iv, data) ->
            if(data != null) {
                iv.setImageResource(R.drawable.ic_check)
                requireContext().copyToClipboard(data, "")
            } else {
                iv.setImageResource(R.drawable.ic_copy)
            }
        })

        viewModel.updatePriceConversion(data.receiveAmount)
        viewModel.priceConversion.observe(viewLifecycleOwner, Observer {
            binding.fiatConversion.text = it
        })

        if(data.paymentTxId?.isNotEmpty() == true) {
            binding.transactionId.text = data.paymentTxId
        } else {
            binding.transactionId.text = getString(R.string.not_applicable)
        }

        binding.memoContainer.isVisible = data.paymentAddressMemo?.isNotEmpty() == true
        binding.memo.text = data.paymentAddressMemo
        binding.smartPay.enableButton(
            data.fromShort.lowercase() == Constants.Strings.BTC_TICKER
                    && data.paymentTxId == null
        )

        binding.smartPay.onClick {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    actionBarTitle = 0,
                    actionBarSubtitle = 0,
                    actionBarVariant = 0,
                    actionLeft = 0,
                    actionRight = 0,
                    configurationType = FragmentConfigurationType.CONFIGURATION_INVOICE,
                    configData = ConfigInvoiceData(
                        amountToSend = data.sendAmount,
                        address = data.paymentAddress,
                        memo = "${data.fromShort.lowercase()} -> ${data.toShort.lowercase()} swap"
                    )
                ),
                Constants.Navigation.WALLET_UUID_BUNDLE_ID to viewModel.getWalletId()
            )

            navigate(
                R.id.invoiceFragment,
                bundle,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        binding.done.onClick {
            findNavController().popBackStack()
        }
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.exchangeDetailsFragment
    }

    override fun actionBarSubtitle(): Int {
        return R.string.swap_details_fragment_label
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
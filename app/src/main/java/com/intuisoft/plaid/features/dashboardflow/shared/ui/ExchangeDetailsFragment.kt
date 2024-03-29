package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.intuisoft.plaid.BuildConfig
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventExchangeDetailsSmartPay
import com.intuisoft.plaid.common.analytics.events.EventExchangeDetailsView
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.databinding.FragmentExchangeDetailsBinding
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.ExchangeDetailsViewModel
import com.intuisoft.plaid.common.model.ExchangeStatus
import com.intuisoft.plaid.common.util.extensions.mapToListOf
import com.intuisoft.plaid.model.ExchangeStatusColors
import com.intuisoft.plaid.util.fragmentconfig.BasicConfigData
import com.intuisoft.plaid.util.fragmentconfig.ConfigInvoiceData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.HashMap

class ExchangeDetailsFragment : ConfigurableFragment<FragmentExchangeDetailsBinding>(pinProtection = true) {
    protected val viewModel: ExchangeDetailsViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val eventTracker: EventTracker by inject()
    protected val billing: BillingManager by inject()

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
        val swapData = configuration!!.configData as BasicConfigData
        val data = Gson().fromJson(swapData.payload, ExchangeInfoDataModel::class.java)
        val status = ExchangeStatus.values().find { it.type == data.status }!!
        val from = localStoreRepository.getSupportedCurrenciesData().filter {
            it.id == data.fromId
        }.firstOrNull()
        val to = localStoreRepository.getSupportedCurrenciesData().filter {
            it.id == data.toId
        }.firstOrNull()

        eventTracker.log(EventExchangeDetailsView())
        binding.status.text = data.status
        binding.status.setTextColor(
            resources.getColor(
                ExchangeStatusColors.getColor(status)
            )
        )
        binding.from.text = data.from + " (${from?.network?.uppercase() ?: "?"})"
        binding.to.text = data.to + " (${to?.network?.uppercase() ?: "?"})"
        binding.exchangeId.text = data.id
        if(status.isFinalState()) {
            binding.sendAmount.text =
                "${SimpleCoinNumberFormat.formatCrypto(data.sendAmount)} ${data.fromShort}"
            binding.receiveAmount.text =
                "~${SimpleCoinNumberFormat.formatCrypto(data.expectedReceiveAmount)} ${data.toShort}"
        } else {
            binding.sendAmount.text =
                "${SimpleCoinNumberFormat.formatCrypto(data.expectedSendAmount)} ${data.fromShort}"
            binding.receiveAmount.text =
                "~${SimpleCoinNumberFormat.formatCrypto(data.expectedReceiveAmount)} ${data.toShort}"
        }
        binding.fiatConversionContainer.isVisible = data.toShort.lowercase() == Constants.Strings.BTC_TICKER
        binding.sendAmount2.text = "${SimpleCoinNumberFormat.formatCrypto(data.expectedSendAmount)} ${data.fromShort}"
        binding.exchangeIssues.setOnSingleClickListener {
            val status = viewModel.getWalletStatus()

            val release = Build.VERSION.RELEASE
            val sdkVersion = Build.VERSION.SDK_INT

            sendEmail(
                to = getString(R.string.business_info_email),
                subject = getString(R.string.business_info_support_request_subject_exchange),
                message = getString(R.string.business_info_support_request_exchange_message,
                    data.id,
                    viewModel.getWallet()!!.testNetWallet.toString(),
                    viewModel.getWallet()!!.hiddenWallet.toString(),
                    status[Constants.Strings.STATUS_INFO_1],
                    status[Constants.Strings.STATUS_INFO_2],
                    status[Constants.Strings.STATUS_INFO_3],
                    status[Constants.Strings.STATUS_INFO_4],
                    status[Constants.Strings.STATUS_INFO_5],
                    viewModel.getWallet()!!.walletKit!!.getConnectedPeersCount()
                        .toLong().mapToListOf {
                            try {
                                val peerInfo = status["${Constants.Strings.STATUS_INFO_6}${it + 1}"] as HashMap<String, String>

                                """
                                    Peer #${it + 1}
                                    Status: ${peerInfo[Constants.Strings.PEER_STATUS_INFO_1]}
                                    Host: ${peerInfo[Constants.Strings.PEER_STATUS_INFO_2]}
                                    Best Block: ${peerInfo[Constants.Strings.PEER_STATUS_INFO_3]}
                                    Tasks: ${peerInfo[Constants.Strings.PEER_STATUS_INFO_4]}
                                """.trimIndent()
                            } catch(t: Throwable) {
                                ""
                            }

                        }.joinToString("\n~\n"),
                    Build.BRAND,
                    "Android SDK: $sdkVersion ($release)",
                    System.getProperty("os.version"),
                    Build.MODEL,
                    Build.PRODUCT,
                    if(viewModel.isProEnabled()) Constants.Strings.PRO_SUBSCRIPTION_MARK + "\n" else "",
                    billing.getUserId(),
                    BuildConfig.VERSION_NAME
                )
            )
        }
        binding.exchangeId.setOnSingleClickListener {
            viewModel.copyDataItemClicked(
                binding.copyExchangeId, data.id
            )
        }
        binding.paymentAddressContainer.setOnSingleClickListener {
            viewModel.copyDataItemClicked(
                binding.copyPaymentAddress, data.paymentAddress
            )
        }
        binding.transactionIdContainer.setOnSingleClickListener {
            if(data.receiveTxId?.isNotEmpty() == true) {
                viewModel.copyDataItemClicked(
                    binding.copyTransactionId, data.receiveTxId!!
                )
            } else if(data.paymentTxId?.isNotEmpty() == true) {
                viewModel.copyDataItemClicked(
                    binding.copyTransactionId, data.paymentTxId!!
                )
            }
        }
        binding.sendAmountContainer.setOnSingleClickListener {
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

        binding.paymentAddress.text = data.paymentAddress
        if(data.receiveTxId?.isNotEmpty() == true) {
            binding.txIdType.text = getString(R.string.swap_details_tx_id_type_2)
            binding.transactionId.text = data.receiveTxId
        } else if(data.paymentTxId?.isNotEmpty() == true) {
            binding.txIdType.text = getString(R.string.swap_details_tx_id_type_1)
            binding.transactionId.text = data.paymentTxId
        } else {
            binding.transactionId.text = getString(R.string.not_applicable)
        }

        binding.memoContainer.isVisible = data.paymentAddressMemo?.isNotEmpty() == true
        binding.memo.text = data.paymentAddressMemo
        binding.smartPay.enableButton(
            data.fromShort.lowercase() == Constants.Strings.BTC_TICKER
                    && data.paymentTxId == null && status == ExchangeStatus.WAITING
        )

        binding.smartPay.onClick {
            eventTracker.log(EventExchangeDetailsSmartPay())
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    configurationType = FragmentConfigurationType.CONFIGURATION_INVOICE,
                    configData = ConfigInvoiceData(
                        amountToSend = if(status.isFinalState()) data.sendAmount else data.expectedSendAmount,
                        address = data.paymentAddress,
                        memo = getString(R.string.exchange_assets_invoice_description, data.fromShort.lowercase(), data.toShort.lowercase()),
                        exchangeId = data.id
                    )
                )
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
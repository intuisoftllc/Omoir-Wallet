package com.intuisoft.plaid.features.dashboardflow.pro.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventAtpCancel
import com.intuisoft.plaid.common.local.db.AssetTransferDao
import com.intuisoft.plaid.common.local.db.BatchDao
import com.intuisoft.plaid.common.model.AssetTransferStatus
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.FragmentAssetTransferDetailsBinding
import com.intuisoft.plaid.features.dashboardflow.pro.adapters.BatchInfoAdapter
import com.intuisoft.plaid.features.dashboardflow.pro.viewmodel.AtpDetailsViewModel
import com.intuisoft.plaid.util.Plural
import com.intuisoft.plaid.util.SimpleTimeFormat
import com.intuisoft.plaid.util.fragmentconfig.BasicConfigData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class AssetTransferDetailsFragment : ConfigurableFragment<FragmentAssetTransferDetailsBinding>(pinProtection = true) {
    protected val viewModel: AtpDetailsViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()
    protected val eventTracker: EventTracker by inject()

    val adapter = BatchInfoAdapter(
        localStoreRepository = localStoreRepository
    ) {
        viewModel.getTransaction(it)?.let { transaction ->
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAssetTransferDetailsBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_ATP
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        val data = configuration!!.configData as BasicConfigData
        viewModel.setTransferId(data.payload)

        walletManager.databaseUpdated.observe(viewLifecycleOwner, Observer {
            when(it) {
                is AssetTransferDao,
                is BatchDao -> {
                    viewModel.getTransferData(showError = false)
                }
            }
        })

        viewModel.getTransferData()
        binding.batches.adapter = adapter
        viewModel.transfer.observe(viewLifecycleOwner, Observer { transfer ->
            showATPStatus(
                requireContext(),
                binding.status,
                transfer.status
            )

            binding.cancel.isVisible = transfer.status.id in AssetTransferStatus.NOT_STARTED.id..AssetTransferStatus.IN_PROGRESS.id
            binding.cancel.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
                eventTracker.log(EventAtpCancel())
                walletManager.cancelTransfer(transfer.id)
                binding.cancel.isVisible = false
            }

            binding.recipient.setSubTitleText(viewModel.getWalletName(transfer.recipientWallet))
            binding.createdAt.setSubTitleText(SimpleTimeFormat.getDateByLocale(transfer.createdAt, Locale.US) ?: getString(R.string.not_applicable))
            binding.batchGap.setSubTitleText(Plural.of("Block", transfer.batchGap.toLong()))
            binding.batchSize.setSubTitleText(Plural.of("Utxo", transfer.batchSize.toLong()))

            val rate = RateConverter(localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0)
            rate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, transfer.expectedAmount.toDouble())
            binding.expectedAmount.setSubTitleText(rate.from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency(), false).second)

            rate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, transfer.sent.toDouble())
            binding.amountSent.setSubTitleText(rate.from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency(), false).second)

            rate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, transfer.feesPaid.toDouble())
            binding.feesPaid.setSubTitleText(rate.from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency(), false).second)
            binding.feeRange.setSubTitleText(getString(R.string.sat_per_vbyte, "${transfer.feeRangeLow}-${transfer.feeRangeHigh}"))
            binding.adjustToNetwork.setSubTitleText(
                if(transfer.dynamicFees)
                    getString(R.string.yes)
                else getString(R.string.no)
            )

            adapter.addOrUpdateItems(localStoreRepository.getBatchDataForTransfer(transfer.id).toArrayList())
        })
    }

    companion object {
        fun showATPStatus(
            context: Context,
            view: TextView,
            status: AssetTransferStatus) {
            when(status) {
                AssetTransferStatus.NOT_STARTED -> {
                    view.text = context.getString(R.string.atp_status_not_started)
                    view.setTextColor(context.getColor(R.color.description_text_color))
                }

                AssetTransferStatus.IN_PROGRESS -> {
                    view.text = context.getString(R.string.atp_status_in_progress)
                    view.setTextColor(context.getColor(R.color.description_text_color))
                }

                AssetTransferStatus.WAITING -> {
                    view.text = context.getString(R.string.atp_status_waiting)
                    view.setTextColor(context.getColor(R.color.description_text_color))
                }

                AssetTransferStatus.PARTIALLY_COMPLETED -> {
                    view.text = context.getString(R.string.atp_status_partially_completed)
                    view.setTextColor(context.getColor(R.color.warning_color))
                }

                AssetTransferStatus.COMPLETED -> {
                    view.text = context.getString(R.string.atp_status_completed)
                    view.setTextColor(context.getColor(R.color.success_color))
                }

                AssetTransferStatus.FAILED -> {
                    view.text = context.getString(R.string.atp_status_failed)
                    view.setTextColor(context.getColor(R.color.error_color))
                }

                AssetTransferStatus.CANCELLED -> {
                    view.text = context.getString(R.string.atp_status_cancelled)
                    view.setTextColor(context.getColor(R.color.error_color))
                }
            }
        }
    }
    
    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun actionBarSubtitle(): Int {
        return R.string.atp_details_fragment_label
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.atpDetailsFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
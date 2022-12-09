package com.intuisoft.plaid.features.dashboardflow.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.local.db.AssetTransferDao
import com.intuisoft.plaid.common.local.db.BatchDao
import com.intuisoft.plaid.common.model.AssetTransferStatus
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.databinding.FragmentAssetTransferDetailsBinding
import com.intuisoft.plaid.features.dashboardflow.viewmodel.AtpDetailsViewModel
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
        viewModel.getTransferData()
       
        viewModel.transfer.observe(viewLifecycleOwner, Observer { transfer ->
            when(transfer.status) {
                AssetTransferStatus.NOT_STARTED -> {
                    binding.status.text = requireContext().getString(R.string.atp_status_not_started)
                    binding.status.setTextColor(requireContext().getColor(R.color.text_grey))
                }

                AssetTransferStatus.IN_PROGRESS -> {
                    binding.status.text = requireContext().getString(R.string.atp_status_in_progress)
                    binding.status.setTextColor(requireContext().getColor(R.color.text_grey))
                }

                AssetTransferStatus.PARTIALLY_COMPLETED -> {
                    binding.status.text = requireContext().getString(R.string.atp_status_partially_completed)
                    binding.status.setTextColor(requireContext().getColor(R.color.warning_color))
                }

                AssetTransferStatus.COMPLETED -> {
                    binding.status.text = requireContext().getString(R.string.atp_status_completed)
                    binding.status.setTextColor(requireContext().getColor(R.color.success_color))
                }

                AssetTransferStatus.FAILED -> {
                    binding.status.text = requireContext().getString(R.string.atp_status_failed)
                    binding.status.setTextColor(requireContext().getColor(R.color.error_color))
                }

                AssetTransferStatus.CANCELLED -> {
                    binding.status.text = getString(R.string.atp_status_cancelled)
                    binding.status.setTextColor(requireContext().getColor(R.color.error_color))
                }
            }
            
            binding.recipient.setSubTitleText(viewModel.getWalletName(transfer.walletId))
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
        })

        walletManager.databaseUpdated.observe(viewLifecycleOwner, Observer {
            when(it) {
                is AssetTransferDao,
                is BatchDao -> {
                    viewModel.getTransferData(showError = false)
                }
            }
        })
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
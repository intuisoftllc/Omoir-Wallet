package com.intuisoft.plaid.features.dashboardscreen.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.databinding.FragmentTransactionDetailsBinding
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.Plural
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.util.SimpleTimeFormat.getDateByLocale
import com.intuisoft.plaid.util.fragmentconfig.ConfigTransactionData
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.models.TransactionType
import io.horizontalsystems.bitcoinkit.BitcoinKit
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class TransactionDetailsFragment : PinProtectedFragment<FragmentTransactionDetailsBinding>() {
    private val viewModel: WalletViewModel by viewModel()
    private val localStoreRepository: LocalStoreRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTransactionDetailsBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(FragmentConfigurationType.CONFIGURATION_TRANSACTION_DATA)
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {

        configuration?.let {
            when(it.configurationType) {
                FragmentConfigurationType.CONFIGURATION_TRANSACTION_DATA -> {
                    val transaction = CommonService.getGsonInstance()
                        .fromJson((it.configData as ConfigTransactionData).payload, TransactionInfo::class.java)

                    when(transaction.type) {
                        TransactionType.Incoming -> {
                            binding.transactionType.setSubTitleText(getString(R.string.transaction_details_type_incoming))
                            binding.transactionType.showSubtitleIcon(R.drawable.ic_incoming)
                        }

                        TransactionType.Outgoing -> {
                            binding.transactionType.setSubTitleText(getString(R.string.transaction_details_type_outgoing))
                            binding.transactionType.showSubtitleIcon(R.drawable.ic_outgoing)
                        }

                        TransactionType.SentToSelf -> {
                            binding.transactionType.setSubTitleText(getString(R.string.transaction_details_type_sent_to_self))
                            binding.transactionType.showSubtitleIcon(R.drawable.ic_sent_to_self)
                        }
                    }

                    if(viewModel.getWalletNetwork() == BitcoinKit.NetworkType.TestNet) {
                        binding.viewOnBlockchain.setButtonText(getString(R.string.transaction_details_view_testnet_transaction_online))
                    }

                    binding.amount.setSubTitleText(SimpleCoinNumberFormat.format(localStoreRepository, transaction.amount, false))
                    binding.transactionDate.setSubTitleText(getDateByLocale(transaction.timestamp * 1000, Locale.US.language)!!)

                    when(transaction.fee) {
                        null -> {
                            binding.transactionFee.setSubTitleText(getString(R.string.not_applicable))
                        }
                        else -> {
                            binding.transactionFee.setSubTitleText(SimpleCoinNumberFormat.format(localStoreRepository, transaction.fee!!, false))
                        }
                    }

                    binding.transactionId.setSubTitleText(transaction.transactionHash)
                    binding.transactionConfirmations.setSubTitleText(Plural.of("Confirmation", viewModel.getConfirmations(transaction).toLong(), "s"))

                    binding.transactionId.onClick {
                        requireContext().copyToClipboard(transaction.transactionHash, "transactionId")
                        styledSnackBar(requireView(), com.intuisoft.plaid.common.util.Constants.Strings.COPIED_TO_CLIPBOARD, true)
                    }

                    when(transaction.blockHeight) {
                        null, 0 -> {
                            if(transaction.status == TransactionStatus.INVALID) {
                                binding.transactionStatus.setSubTitleText(getString(R.string.transaction_details_status_invalid))
                            } else {
                                binding.transactionStatus.setSubTitleText(getString(R.string.transaction_details_status_pending))
                            }
                        }
                        else -> {
                            if(viewModel.getConfirmations(transaction) >= localStoreRepository.getMinimumConfirmations()) {
                                binding.transactionStatus.setSubTitleText(getString(R.string.transaction_details_status_processed))
                            } else {
                                binding.transactionStatus.setSubTitleText(getString(R.string.transaction_details_status_pending))
                            }
                        }
                    }

                    binding.close.setOnClickListener {
                        findNavController().popBackStack()
                    }

                    binding.share.onClick {

                        val text = getString(
                            R.string.transaction_details_share_text,
                            SimpleCoinNumberFormat.format(localStoreRepository, transaction.amount),
                            if(transaction.fee != null) SimpleCoinNumberFormat.format(localStoreRepository, transaction.fee!!) else "N/A",
                            if(viewModel.getWalletNetwork() == BitcoinKit.NetworkType.TestNet) com.intuisoft.plaid.common.util.Constants.Strings.BLOCK_CYPHER_TX_URL + transaction.transactionHash
                            else com.intuisoft.plaid.common.util.Constants.Strings.BLOCK_CYPHER_TX_URL + transaction.transactionHash
                        )
                        val subject = getString(R.string.transaction_details_share_tag)
                        requireActivity().shareText(subject, text)
                    }

                    binding.viewOnBlockchain.onClick {
                        if(viewModel.getWalletNetwork() == BitcoinKit.NetworkType.MainNet)
                            viewOnBlockchainCom(Constants.Strings.BLOCKCHAIN_COM_TX_URL, transaction.transactionHash)
                        else
                            viewOnBlockchainCom(Constants.Strings.BLOCK_CYPHER_TX_URL, transaction.transactionHash)
                    }
                }
            }
        }

    }

    fun viewOnBlockchainCom(url: String, txId: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("$url$txId"))
        startActivity(browserIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun navigationId(): Int {
        return R.id.transactionDetailsFragment
    }
}
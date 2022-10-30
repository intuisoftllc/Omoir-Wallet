package com.intuisoft.plaid.features.dashboardscreen.ui

import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentTransactionDetailsBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WalletExportViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.util.SimpleTimeFormat.getDateByLocale
import com.intuisoft.plaid.util.fragmentconfig.ConfigTransactionData
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.models.TransactionStatus
import io.horizontalsystems.bitcoincore.models.TransactionType
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class TransactionDetailsFragment : PinProtectedFragment<FragmentTransactionDetailsBinding>() {
    private val viewModel: WalletExportViewModel by viewModel()
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

//        configuration?.let {
//            when(it.configurationType) {
//                FragmentConfigurationType.CONFIGURATION_TRANSACTION_DATA -> {
//                    val transaction = Gson().fromJson((it.configData as ConfigTransactionData).payload, TransactionInfo::class.java)
//
//                    when(transaction.type) {
//                        TransactionType.Incoming -> {
//                            binding.transactionType.setSubTitleText("Incoming")
//                            binding.transactionType.showSubtitleIcon(R.drawable.ic_recieve_coins_48)
//                        }
//
//                        TransactionType.Outgoing -> {
//                            binding.transactionType.setSubTitleText("Outgoing")
//                            binding.transactionType.showSubtitleIcon(R.drawable.ic_send_coins_48)
//                        }
//
//                        TransactionType.SentToSelf -> {
//                            binding.transactionType.setSubTitleText("Sent To Self")
//                            binding.transactionType.showSubtitleIcon(R.drawable.ic_sent_coins_to_self_48)
//                        }
//                    }
//
//                    binding.amount.setSubTitleText(SimpleCoinNumberFormat.format(localStoreRepository, transaction.amount, true))
//                    binding.transactionDate.setSubTitleText(getDateByLocale(transaction.timestamp * 1000, Locale.US.language)!!)
//
//                    when(transaction.fee) {
//                        null -> {
//                            binding.transactionFee.setSubTitleText("N/A")
//                        }
//                        else -> {
//                            binding.transactionFee.setSubTitleText(SimpleCoinNumberFormat.format(localStoreRepository, transaction.fee!!, true))
//                        }
//                    }
//
//                    binding.transactionId.setSubTitleText(transaction.transactionHash)
//
//                    binding.transactionId.onClick {
//                        requireContext().copyToClipboard(transaction.transactionHash, "transactionId")
//                        styledSnackBar(requireView(), Constants.Strings.COPIED_TO_CLIPBOARD, true)
//                    }
//
//                    when(transaction.blockHeight) {
//                        null, 0 -> {
//                            if(transaction.status == TransactionStatus.INVALID) {
//                                binding.transactionStatus.setSubTitleText("Invalid")
//                            } else {
//                                binding.transactionStatus.setSubTitleText("Pending")
//                            }
//                        }
//                        else -> {
//                            binding.transactionStatus.setSubTitleText("Processed")
//                        }
//                    }
//
//                    binding.close.setOnClickListener {
//                        findNavController().popBackStack()
//                    }
//
//                    binding.share.onClick {
//
//                        val text = """
//                                    Check out this transaction for amount: ${SimpleCoinNumberFormat.format(localStoreRepository, transaction.amount)}, with fee: ${SimpleCoinNumberFormat.format(localStoreRepository, transaction.fee!!, true)}.
//                                    You can view this transaction at: ${Constants.Strings.BLOCKCHAIN_COM_TX_URL}${transaction.transactionHash}
//                                    """
//                        val subject = "Bitcoin Transaction"
//                        requireActivity().shareText(subject, text)
//                    }
//
//                    binding.viewOnBlockchain.onClick {
//                        viewOnBlockchainCom(Constants.Strings.BLOCKCHAIN_COM_TX_URL, transaction.transactionHash)
//                    }
//                }
//            }
//        }

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
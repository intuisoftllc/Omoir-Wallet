package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.delegates.DelegateManager
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.databinding.FragmentInvoiceBinding
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.InvoiceViewModel
import com.intuisoft.plaid.listeners.BarcodeResultListener
import com.intuisoft.plaid.util.fragmentconfig.ConfigInvoiceData
import com.intuisoft.plaid.util.fragmentconfig.SendFundsData
import com.intuisoft.plaid.common.delegates.wallet.WalletDelegate
import io.horizontalsystems.bitcoincore.models.BitcoinPaymentData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class InvoiceFragment : ConfigurableFragment<FragmentInvoiceBinding>(pinProtection = true), BarcodeResultListener {
    protected val viewModel: InvoiceViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: WalletDelegate by inject()
    protected val delegateManager: DelegateManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentInvoiceBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_INVOICE
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        val data = configuration?.configData as? ConfigInvoiceData

        binding.next.enableButton(false)

        if(data != null) {
            viewModel.loadInvoice(
                BitcoinPaymentData(
                    address = data.address,
                    amount = data.amountToSend,
                    label = data.memo
                ).uriPaymentAddress
            )

            viewModel.setExchangeId(data.exchangeId)
            binding.scanInvoice.enableButton(false)
        }

        binding.scanInvoice.onClick {
            scanInvoice()
        }

        viewModel.updateAvailableBalance()
        viewModel.invoiceLoaded.observe(viewLifecycleOwner, Observer {
            binding.amountToSpend.setSubTitleText(it.amount)
            binding.description.setSubTitleText(it.description)
            binding.address.setSubTitleText(it.address)
        })

        viewModel.onDisplayExplanation.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), it, true)
        })

        viewModel.enableNext.observe(viewLifecycleOwner, Observer {
            binding.next.enableButton(true)
        })

        viewModel.onNextStep.observe(viewLifecycleOwner, Observer {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    configurationType = FragmentConfigurationType.CONFIGURATION_WITHDRAW,
                    configData = SendFundsData(
                        amountToSend = viewModel.getSatsToSpend(),
                        spendFrom = viewModel.selectedUTXOs.map { it.output.address!! },
                        address = it.address,
                        exchangeId = it.exchangeId,
                        memo = it.description,
                        invoiceSend = true
                    )
                )
            )

            navigate(
                R.id.withdrawConfirmtionFragment,
                bundle,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        })

        binding.availableBalance.onClick {
            WithdrawalFragment.showCoinControlBottomSheet(
                context = requireContext(),
                showHint = true,
                getUnspentOutputs = {
                    viewModel.getUnspentOutputs()
                },
                getSelectedUTXOs = {
                    viewModel.selectedUTXOs
                },
                updateSelectedUTXOs = {
                    viewModel.updateUTXOs(it.toMutableList())
                },
                localStoreRepository = localStoreRepository,
                delegateManager = delegateManager,
                getFullKeyPath = {
                    walletManager.getFullPublicKeyPath(it)
                },
                addSingleUTXO = {
                    viewModel.addSingleUTXO(it)
                },
                addToStack = ::addToStack,
                removeFromStack = ::removeFromStack
            )
        }

        viewModel.onAvailableBalanceUpdated.observe(viewLifecycleOwner, Observer {
            binding.availableBalance.setSubTitleText(it)
        })

        binding.next.onClick {
            viewModel.onNextStep()
        }
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.invoiceFragment
    }

    override fun actionBarSubtitle(): Int {
        return R.string.invoice_fragment_label
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

    override fun onAddressReceived(invoice: String) {
        viewModel.loadInvoice(invoice)
    }
}
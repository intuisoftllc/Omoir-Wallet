package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.databinding.FragmentWithdrawBinding
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.WithdrawalViewModel
import com.intuisoft.plaid.features.homescreen.adapters.CoinControlAdapter
import com.intuisoft.plaid.listeners.StateListener
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.common.util.extensions.safeWalletScope
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.features.dashboardflow.pro.ui.UtxoDistributionFragment
import com.intuisoft.plaid.util.fragmentconfig.BasicConfigData
import com.intuisoft.plaid.util.fragmentconfig.SendFundsData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.PublicKey
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class WithdrawalFragment : ConfigurableFragment<FragmentWithdrawBinding>(pinProtection = true), StateListener {
    private val viewModel: WithdrawalViewModel by viewModel()
    private val localStoreRepository: LocalStoreRepository by inject()
    private val walletManager: AbstractWalletManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWithdrawBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_SPEND_COIN
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        when (configuration?.configurationType) {
            FragmentConfigurationType.CONFIGURATION_SPEND_COIN -> {
                val data = configuration!!.configData as BasicConfigData
                viewModel.setInitialSpendUtxo(data.payload)
            }
        }

        viewModel.addWalletStateListener(this)
        viewModel.showWalletDisplayUnit()
        viewModel.refreshLocalCache()

        binding.currency.setButtonText(SimpleCurrencyFormat.getSymbol(localStoreRepository.getLocalCurrency()))
        viewModel.walletDisplayUnit.observe(viewLifecycleOwner, Observer {
            when(it) {
                BitcoinDisplayUnit.BTC -> {
                    binding.btc.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                    binding.sats.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    binding.currency.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                }

                BitcoinDisplayUnit.SATS -> {
                    binding.btc.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    binding.sats.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                    binding.currency.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                }

                BitcoinDisplayUnit.FIAT -> {
                    binding.btc.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    binding.sats.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    binding.currency.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                }
            }

            viewModel.displaySpendAmount()
            viewModel.displayTotalBalance()
        })

        viewModel.localSpendAmount.observe(viewLifecycleOwner, Observer {
            binding.amount.text = it
        })

        binding.btc.onClick {
            viewModel.changeDisplayUnit(BitcoinDisplayUnit.BTC)
            viewModel.showWalletDisplayUnit()
        }

        binding.sats.onClick {
            viewModel.changeDisplayUnit(BitcoinDisplayUnit.SATS)
            viewModel.showWalletDisplayUnit()
        }

        binding.currency.onClick {
            viewModel.changeDisplayUnit(BitcoinDisplayUnit.FIAT)
            viewModel.showWalletDisplayUnit()
        }

        viewModel.maximumSpend.observe(viewLifecycleOwner, Observer {
            binding.availableBalance.setSubTitleText(it)
        })

        binding.availableBalance.onClick {
            showCoinControlBottomSheet(
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

        viewModel.shouldAdvance.observe(viewLifecycleOwner, Observer {
            binding.next.enableButton(it)
        })

        binding.number0.setOnClickListener {
            viewModel.increaseBy(0)
        }

        binding.number1.setOnClickListener {
            viewModel.increaseBy(1)
        }

        binding.number2.setOnClickListener {
            viewModel.increaseBy(2)
        }

        binding.number3.setOnClickListener {
            viewModel.increaseBy(3)
        }

        binding.number4.setOnClickListener {
            viewModel.increaseBy(4)
        }

        binding.number5.setOnClickListener {
            viewModel.increaseBy(5)
        }

        binding.number6.setOnClickListener {
            viewModel.increaseBy(6)
        }

        binding.number7.setOnClickListener {
            viewModel.increaseBy(7)
        }

        binding.number8.setOnClickListener {
            viewModel.increaseBy(8)
        }

        binding.number9.setOnClickListener {
            viewModel.increaseBy(9)
        }

        binding.deleteAll.setOnClickListener {
            viewModel.decreaseBy(false)
        }

        binding.back.setOnClickListener {
            viewModel.decreaseBy(true)
        }

        binding.dot.setOnClickListener {
            viewModel.activateDecimalEntry()
        }
        
        viewModel.onInputRejected.observe(viewLifecycleOwner, Observer {
            onAmountOverBalanceAnimation()
        })

        viewModel.onDisplayExplanation.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), it, true)
        })

        binding.sendMax.setOnClickListener {
            viewModel.spendMaxBalance()
        }

        viewModel.onNextStep.observe(viewLifecycleOwner, Observer {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    actionBarTitle = 0,
                    actionBarSubtitle = 0,
                    actionBarVariant = 0,
                    actionLeft = 0,
                    actionRight = 0,
                    configurationType = FragmentConfigurationType.CONFIGURATION_WITHDRAW,
                    configData = SendFundsData(
                        amountToSend = viewModel.getSatsToSpend(),
                        spendFrom = viewModel.selectedUTXOs.map { it.output.address!! }
                    )
                )
            )

            navigate(
                R.id.withdrawConfirmtionFragment,
                bundle,
                ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        })

        binding.next.onClick {
            viewModel.onNextStep()
        }
    }

    override fun onWalletStateUpdated(wallet: LocalWalletModel) {
        MainScope().launch {
            safeWalletScope {
                if (wallet.uuid == viewModel.getWalletId()) {
                    viewModel.showWalletDisplayUnit()
                }
            }
        }
    }

    override fun onWalletAlreadySynced(wallet: LocalWalletModel) {
        // ignore
    }

    companion object {
        fun showCoinControlBottomSheet(
            context: Context,
            showHint: Boolean,
            getUnspentOutputs: () -> List<UnspentOutput>,
            getSelectedUTXOs: () -> List<UnspentOutput>,
            updateSelectedUTXOs: (List<UnspentOutput>) -> Unit,
            addSingleUTXO: (UnspentOutput) -> Unit,
            getFullKeyPath: (PublicKey) -> String,
            localStoreRepository: LocalStoreRepository,
            addToStack: (AppCompatDialog, () -> Unit) -> Unit,
            removeFromStack: (AppCompatDialog) -> Unit
        ) {
            val bottomSheetDialog = BottomSheetDialog(context)
            var blockDialogRecreate = false
            addToStack(bottomSheetDialog) {
                blockDialogRecreate = true
            }

            bottomSheetDialog.setContentView(R.layout.bottom_sheet_coin_control)
            val selectAll = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.select_all)!!
            val noUTXOs = bottomSheetDialog.findViewById<TextView>(R.id.no_utxos)!!
            val hint = bottomSheetDialog.findViewById<TextView>(R.id.hint)
            val unspentOutputsList = bottomSheetDialog.findViewById<RecyclerView>(R.id.utxos)!!
            val utxos = getUnspentOutputs().sortedBy { it.output.value }.reversed()

            var itemClickCount = 0

            if (utxos.isEmpty()) {
                selectAll.enableButton(false)
                noUTXOs.isVisible = true
                unspentOutputsList.isVisible = false
            } else {
                MainScope().launch {
                    if(showHint) {
                        hint?.isVisible = true
                        delay(2 * Constants.Time.MILLS_PER_SEC.toLong())
                        hint?.isVisible = false
                    }
                }

                val adapter = CoinControlAdapter(
                    localStoreRepository = localStoreRepository,
                    onAllItemsSelected = {
                        if (it) {
                            selectAll.setButtonStyle(RoundedButtonView.ButtonStyle.ROUNDED_STYLE)
                        } else {
                            selectAll.setButtonStyle(RoundedButtonView.ButtonStyle.OUTLINED_STYLE)
                        }
                    },
                    onItemLongClicked = {
                        bottomSheetDialog.cancel()
                        UtxoDistributionFragment.showCoinInfoBottomSheet(
                            context = context,
                            coin = it,
                            localStoreRepository = localStoreRepository,
                            getFullKeyPath = getFullKeyPath,
                            onDismiss = {
                                if(!blockDialogRecreate) {
                                    showCoinControlBottomSheet(
                                        context = context,
                                        showHint = false,
                                        getUnspentOutputs = getUnspentOutputs,
                                        getSelectedUTXOs = getSelectedUTXOs,
                                        updateSelectedUTXOs = updateSelectedUTXOs,
                                        addSingleUTXO = addSingleUTXO,
                                        getFullKeyPath = getFullKeyPath,
                                        localStoreRepository = localStoreRepository,
                                        addToStack = addToStack,
                                        removeFromStack = removeFromStack
                                    )
                                }
                            },
                            onSpendCoin = {
                                addSingleUTXO(it)
                            },
                            addToStack = addToStack,
                            removeFromStack = removeFromStack
                        )
                    }
                )

                unspentOutputsList.adapter = adapter
                adapter.addUTXOs(utxos.toArrayList(), getSelectedUTXOs().toArrayList())

                selectAll.onClick {
                    adapter.selectAll(!adapter.areAllItemsSelected())
                }

                bottomSheetDialog.setOnCancelListener {
                    removeFromStack(bottomSheetDialog)
                    updateSelectedUTXOs(adapter.selectedUTXOs)
                }
            }

            bottomSheetDialog.show()
        }
    }

    fun onAmountOverBalanceAnimation() {
        val shake: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.shake)
        binding.amount.startAnimation(shake)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeWalletSyncListener(this)
        _binding = null
    }

    override fun navigationId(): Int {
        return R.id.withdrawalFragment
    }
}
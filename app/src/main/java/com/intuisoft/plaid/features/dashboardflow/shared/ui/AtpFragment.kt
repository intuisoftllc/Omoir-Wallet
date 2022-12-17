package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.FragmentAtpBinding
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.AtpViewModel
import com.intuisoft.plaid.features.homescreen.adapters.UtxoTransfersAdapter
import com.intuisoft.plaid.util.Plural
import com.intuisoft.plaid.util.SimpleTimeFormat
import com.intuisoft.plaid.util.fragmentconfig.BasicConfigData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class AtpFragment : ConfigurableFragment<FragmentAtpBinding>(pinProtection = true) {
    protected val viewModel: AtpViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAtpBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        onBackPressedCallback {
            onNavigateBottomBarSecondaryFragmentBackwards(localStoreRepository)
        }

        viewModel.getInitialWallet()
        viewModel.updateValues()

        viewModel.maxSpend.observe(viewLifecycleOwner, Observer {
            binding.transferAmount.setTitleText(it)
        })

        viewModel.contentNotAvailable.observe(viewLifecycleOwner, Observer {
            activateContentUnavailable(true, getString(R.string.content_not_available_2))
        })

        viewModel.batchGap.observe(viewLifecycleOwner, Observer {
            binding.batchGap.setSubTitleText(it)
        })

        viewModel.batchSize.observe(viewLifecycleOwner, Observer {
            binding.batchSize.setSubTitleText(it)
        })

        viewModel.feeSpread.observe(viewLifecycleOwner, Observer {
            binding.feeSpread.setSubTitleText(it)
        })

        viewModel.displayRecipient.observe(viewLifecycleOwner, Observer {
            binding.recipient.setTitleText(it.name)
        })

        viewModel.noWallets.observe(viewLifecycleOwner, Observer {
            binding.recipient.setTitleText(getString(R.string.not_applicable))
        })

        viewModel.nextEnabled.observe(viewLifecycleOwner, Observer {
            binding.next.enableButton(it)
        })

        binding.next.onClick {
            viewModel.confirmAssetTransfer()
        }

        viewModel.onDisplayExplanation.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), it, true)
        })

        viewModel.confirmTransfer.observe(viewLifecycleOwner, Observer {
            showConfirmTransactionDialog(it)
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            binding.loading.isVisible = it
        })

        viewModel.creatingTransfer.observe(viewLifecycleOwner, Observer {
            activateAnimatedLoading(true, getString(R.string.atp_creating_transfer))
        })

        viewModel.transferCreated.observe(viewLifecycleOwner, Observer {
            activateAnimatedLoading(false, "")
            walletManager.synchronizeAll(false)

            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    configurationType = FragmentConfigurationType.CONFIGURATION_ATP,
                    configData = BasicConfigData(
                        payload = it
                    )
                )
            )

            navigate(
                R.id.atpDetailsFragment,
                bundle,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        })

        binding.recipient.onClick {
            WithdrawConfirmationFragment.showSendToWalletBottomSheet(
                context = requireContext(),
                localStoreRepository = localStoreRepository,
                canTransferToWallet = {
                    viewModel.canTransferToWallet(it)
                },
                onWalletSelected = {
                    viewModel.setWallet(it)
                },
                getWallets = {
                    viewModel.getWallets()
                },
                addToStack = ::addToStack,
                removeFromStack = ::removeFromStack
            )
        }

        binding.transferAmount.onClick {
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

        binding.feeSpread.onClick {
            feeSpreadDialog()
        }

        binding.batchGap.onClick {
            displayBatchGapInfo()
        }

        binding.batchSize.onClick {
            displayBatchSizeInfo()
        }
    }

    fun utxoTransfersDialog(items: List<AtpViewModel.UtxoData>, onCancel: ()-> Unit) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        var blockDialogRecreate = false
        addToStack(bottomSheetDialog) {
            blockDialogRecreate = true
        }
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_atp_utxos_transfers)
        val utxos = bottomSheetDialog.findViewById<RecyclerView>(R.id.utxos)!!

        val adapter = UtxoTransfersAdapter()
        utxos.adapter = adapter
        adapter.addUtxos(items.toArrayList())

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)

            if(!blockDialogRecreate) {
                onCancel()
            }
        }

        bottomSheetDialog.show()
    }

    private fun showConfirmTransactionDialog(data: AtpViewModel.TransferData) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        addToStack(bottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_confirm_asset_transfer)
        val to = bottomSheetDialog.findViewById<SettingsItemView>(R.id.to)
        val batchGap = bottomSheetDialog.findViewById<SettingsItemView>(R.id.batch_gap)
        val batchSize = bottomSheetDialog.findViewById<SettingsItemView>(R.id.batch_size)
        val time = bottomSheetDialog.findViewById<SettingsItemView>(R.id.time)
        val fees = bottomSheetDialog.findViewById<SettingsItemView>(R.id.fees)
        val amount = bottomSheetDialog.findViewById<SettingsItemView>(R.id.amount)
        val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)
        val confirm = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.confirm)

        val rate = RateConverter(0.0)
        val utxosSkipped = !data.utxos.all { it.totalFee > 0 }
        to?.setSubTitleText(data.to.name)
        if(data.batchPenalty == 0)
            batchGap?.setSubTitleText(Plural.of("Block", data.batchGap.toLong()))
        else {
            batchGap?.setSubTitleText(getString(R.string.atp_confirm_batch_size_with_penalty, Plural.of("Block", data.batchGap.toLong()), data.batchPenalty.toString()))
        }
        batchSize?.setSubTitleText(
            if(data.batchSize != viewModel.getBatchSize())
                getString(R.string.atp_confirm_batch_size_adjusted, Plural.of("utxo", data.batchSize.toLong()))
            else Plural.of("utxo", data.batchSize.toLong())
        )
        time?.setSubTitleText("~${SimpleTimeFormat.timeToString(System.currentTimeMillis()
                + ((data.estimatedCompletionTime * Constants.Time.ONE_MINUTE) * Constants.Time.MILLS_PER_SEC), "")}")

        rate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, data.estimatedFee.toDouble())
        fees?.setSubTitleText(
            if(utxosSkipped) getString(R.string.atp_confirm_skipped_utxos, rate.from(RateConverter.RateType.SATOSHI_RATE, "", false).second)
            else rate.from(RateConverter.RateType.SATOSHI_RATE, "", false).second
        )

        rate.setLocalRate(RateConverter.RateType.SATOSHI_RATE, data.sendAmount.toDouble())
        amount?.setSubTitleText(rate.from(RateConverter.RateType.SATOSHI_RATE, "", false).second)

        fees?.onClick {
            bottomSheetDialog.dismiss()
            utxoTransfersDialog(data.utxos) {
                showConfirmTransactionDialog(data)
            }
        }

        confirm?.onClick {
            if(viewModel.isFingerprintEnabled()) {
                validateFingerprint(
                    title = Constants.Strings.USE_BIOMETRIC_AUTH,
                    subTitle = Constants.Strings.USE_BIOMETRIC_REASON_6,
                    onSuccess = {
                        bottomSheetDialog.dismiss()
                        viewModel.createTransfer(data)
                    }
                )
            } else {
                bottomSheetDialog.dismiss()
                viewModel.createTransfer(data)
            }
        }

        cancel?.onClick {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
        }
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }

    fun displayBatchGapInfo() {
        numberPickerDialog(
            title = getString(R.string.atp_setting_3_title),
            initialValue = viewModel.getBatchGap(),
            min = Constants.Limit.MIN_BATCH_GAP,
            max = Constants.Limit.MAX_BATCH_GAP,
            onValueChanged = {
                viewModel.setBatchGap(it)
            },
            onDisplayInfo = {
                batchInfoDialog(
                    layoutId = R.layout.bottom_sheet_atp_batch_gap_info,
                    onCanceled = {
                        displayBatchGapInfo()
                    }
                )
            }
        )
    }

    fun displayBatchSizeInfo() {
        numberPickerDialog(
            title = getString(R.string.atp_setting_4_title),
            initialValue = viewModel.getBatchSize(),
            min = Constants.Limit.MIN_BATCH_SIZE,
            max = Constants.Limit.MAX_BATCH_SIZE,
            onValueChanged = {
                viewModel.setBatchSize(it)
            },
            onDisplayInfo = {
                batchInfoDialog(
                    layoutId = R.layout.bottom_sheet_atp_batch_size_info,
                    onCanceled = {
                        displayBatchSizeInfo()
                    }
                )
            }
        )
    }

    fun numberPickerDialog(
        title: String,
        initialValue: Int,
        min: Int,
        max: Int,
        onValueChanged: (Int) -> Unit,
        onDisplayInfo: (() -> Unit)? = null
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        addToStack(bottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_atp_batch_settings)
        val sheetTitle = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val alert = bottomSheetDialog.findViewById<ImageView>(R.id.alert)
        val numberPicker = bottomSheetDialog.findViewById<NumberPicker>(R.id.numberPicker)

        sheetTitle?.text = title
        numberPicker?.minValue = min
        numberPicker?.maxValue = max
        numberPicker?.value = initialValue
        numberPicker?.wrapSelectorWheel = true
        alert?.isVisible = onDisplayInfo != null
        numberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
            onValueChanged(newVal)
        }

        alert?.setOnClickListener {
            bottomSheetDialog.cancel()
            onDisplayInfo?.invoke()
        }

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
        }
        bottomSheetDialog.show()
    }

    fun feeSpreadDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        var blockDialogRecreate = false
        addToStack(bottomSheetDialog) {
            blockDialogRecreate = true
        }

        bottomSheetDialog.setContentView(R.layout.bottom_sheet_atp_fee_spread)
        val min = bottomSheetDialog.findViewById<SettingsItemView>(R.id.atp_fee_spread_min)
        val max = bottomSheetDialog.findViewById<SettingsItemView>(R.id.atp_fee_spread_max)
        val auto = bottomSheetDialog.findViewById<SettingsItemView>(R.id.atp_fee_auto)
        val alert = bottomSheetDialog.findViewById<ImageView>(R.id.alert)

        var spread = viewModel.getFeeSpread()
        min?.setSubTitleText(spread.first.toString())
        max?.setSubTitleText(spread.last.toString())
        auto?.setSwitchChecked(viewModel.isUsingDynamicBatchNetworkFee())

        min?.onClick {
            numberPickerDialog(
                title = getString(R.string.atp_fee_spread_min),
                initialValue = spread.first,
                min = Constants.Limit.MIN_FEE,
                max = Constants.Limit.MAX_FEE,
                onValueChanged = {
                    if(it > spread.last) {
                        spread = it .. it
                    } else {
                        spread = it .. spread.last
                    }

                    min?.setSubTitleText(spread.first.toString())
                    max?.setSubTitleText(spread.last.toString())
                    viewModel.setFeeSpread(spread)
                    viewModel.updateValues()
                }
            )
        }

        max?.onClick {
            numberPickerDialog(
                title = getString(R.string.atp_fee_spread_max),
                initialValue = spread.last,
                min = spread.first,
                max = Constants.Limit.MAX_FEE,
                onValueChanged = {
                    if(it < spread.first) {
                        spread = it .. it
                    } else {
                        spread = spread.first .. it
                    }

                    min?.setSubTitleText(spread.first.toString())
                    max.setSubTitleText(spread.last.toString())
                    viewModel.setFeeSpread(spread)
                    viewModel.updateValues()
                }
            )
        }

        alert?.setOnClickListener {
            bottomSheetDialog.cancel()
            val dialog = BottomSheetDialog(requireContext())
            addToStack(dialog)
            dialog.setContentView(R.layout.bottom_sheet_atp_fee_spread_info)

            dialog.setOnCancelListener {
                removeFromStack(dialog)

                if(!blockDialogRecreate) {
                    feeSpreadDialog()
                }
            }

            dialog.show()
        }

        auto?.onSwitchClicked {
            viewModel.setUsingDynamicBatchNetworkFee(it)
            viewModel.updateValues()
        }

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
        }
        bottomSheetDialog.show()
    }


    fun batchInfoDialog(
        layoutId: Int,
        onCanceled: () -> Unit
    ) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        var blockDialogRecreate = false
        addToStack(bottomSheetDialog) {
            blockDialogRecreate = true
        }

        bottomSheetDialog.setContentView(layoutId)
        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
            if(!blockDialogRecreate) {
                onCanceled()
            }
        }

        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }

    override fun onNavigateTo(destination: Int) {
        navigate(destination)
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.atpFragment
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_alert_red
    }

    override fun onActionLeft() {
        navigate(
            R.id.atpInfoFragment
        )
    }

    override fun actionBarActionRight(): Int {
        if(!viewModel.isReadOnly())
            return R.drawable.ic_clock
        else return 0
    }

    override fun onActionRight() {
        navigate(
            R.id.atpHistoryFragment
        )
    }

    override fun actionBarSubtitle(): Int {
        return R.string.atp
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
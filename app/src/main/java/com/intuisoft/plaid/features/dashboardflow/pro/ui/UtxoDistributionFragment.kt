package com.intuisoft.plaid.features.dashboardflow.pro.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.FragmentUtxoDistroReportBinding
import com.intuisoft.plaid.features.dashboardflow.pro.viewmodel.UtxoDistributionViewModel
import com.intuisoft.plaid.features.homescreen.adapters.BasicCoinAdapter
import com.intuisoft.plaid.util.Plural
import com.intuisoft.plaid.util.SimpleTimeFormat
import com.intuisoft.plaid.util.fragmentconfig.BasicConfigData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.models.PublicKey
import io.horizontalsystems.bitcoincore.storage.UnspentOutput
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale

class UtxoDistributionFragment : ConfigurableFragment<FragmentUtxoDistroReportBinding>(pinProtection = true) {
    protected val viewModel: UtxoDistributionViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()

    private val adapter = BasicCoinAdapter(
        onCoinSelected = ::onCoinSelected,
        localStoreRepository = localStoreRepository
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUtxoDistroReportBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_INFLOW_REPORT,
                FragmentConfigurationType.CONFIGURATION_OUTFLOW_REPORT,
                FragmentConfigurationType.CONFIGURATION_FEE_REPORT
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {

        binding.chart.sizeConfig = BarChartSizeConfiguration.CHART_SIZE_MEDIUM
        viewModel.getCoins()
        viewModel.barData.observe(viewLifecycleOwner, Observer {
            binding.loading.isVisible = false

            if(it != null) {
                binding.chart.data =
                    it.items.map { item ->
                        item.barName to item.utxos.size.toFloat()
                    }.toArrayList()
            } else {
                binding.chart.data = listOf()
            }
        })

        binding.chart.listener = object : BarSelectedListener {
            override fun onSelected(index: Int) {
                viewModel.onBarSelected(true, index)
            }

            override fun onDeSelected(index: Int) {
                viewModel.onBarSelected(false, index)
            }

        }

        viewModel.median.observe(viewLifecycleOwner, Observer {
            binding.medianValue.text = it
        })

        viewModel.unspentOutputs.observe(viewLifecycleOwner, Observer {
            binding.noTransactionsIcon.isVisible = it.isEmpty()
            binding.noTransactionsMessage.isVisible = it.isEmpty()
            binding.coins.isVisible = it.isNotEmpty()
            binding.totalCoins.text = Plural.of("Coin", it.size.toLong())

            adapter.addCoins(it.toArrayList())
            binding.coins.adapter = adapter
        })

        viewModel.noData.observe(viewLifecycleOwner, Observer {
            binding.noData.isVisible = it
            binding.loading.isVisible = false
        })
    }

    fun onCoinSelected(coin: UnspentOutput) {
        showCoinInfoBottomSheet(
            context = requireContext(),
            coin = coin,
            localStoreRepository = localStoreRepository,
            getFullKeyPath = {
                walletManager.getFullPublicKeyPath(it)
            },
            onSpendCoin = {
                var bundle = bundleOf(
                    Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                        configurationType = FragmentConfigurationType.CONFIGURATION_SPEND_COIN,
                        configData = BasicConfigData(
                            payload = it.output.address!!
                        )
                    )
                )

                navigate(
                    R.id.withdrawalFragment,
                    bundle,
                    Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
                )
            },
            addToStack = ::addToStack,
            removeFromStack = ::removeFromStack
        )
    }

    companion object {
        fun showCoinInfoBottomSheet(
            context: Context,
            coin: UnspentOutput,
            localStoreRepository: LocalStoreRepository,
            getFullKeyPath: (PublicKey) -> String,
            onSpendCoin: ((UnspentOutput) -> Unit)? = null,
            onDismiss: (() -> Unit)? = null,
            addToStack: (AppCompatDialog, () -> Unit) -> Unit,
            removeFromStack: (AppCompatDialog) -> Unit
        ) {
            val bottomSheetDialog = BottomSheetDialog(context)
            var blockDialogRecreate = false
            addToStack(bottomSheetDialog) {
                blockDialogRecreate = true
            }

            bottomSheetDialog.setContentView(R.layout.bottom_sheet_coin_info)
            val spend = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.spend)!!
            val path = bottomSheetDialog.findViewById<TextView>(R.id.info_path)!!
            val address = bottomSheetDialog.findViewById<TextView>(R.id.info_address)!!
            val addressCopyIcon = bottomSheetDialog.findViewById<ImageView>(R.id.copy_address)
            val addressContainer = bottomSheetDialog.findViewById<LinearLayout>(R.id.address_container)!!
            val date = bottomSheetDialog.findViewById<TextView>(R.id.info_date)!!
            val balance = bottomSheetDialog.findViewById<TextView>(R.id.info_amount)!!
            val fiat = bottomSheetDialog.findViewById<TextView>(R.id.info_fiat_conversion)!!
            val rateConverter = RateConverter(
                localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0
            )
            rateConverter.setLocalRate(RateConverter.RateType.SATOSHI_RATE, coin.output.value.toDouble())

            path.text = getFullKeyPath(coin.publicKey)
            address.text = coin.output.address
            date.text = SimpleTimeFormat.getDateByLocale(coin.transaction.timestamp * Constants.Time.MILLS_PER_SEC, Locale.US)
            balance.text = rateConverter.from(RateConverter.RateType.SATOSHI_RATE, "", false).second
            fiat.text = rateConverter.from(RateConverter.RateType.FIAT_RATE, localStoreRepository.getLocalCurrency()).second

            addressContainer.setOnSingleClickListener {
                MainScope().launch {
                    addressCopyIcon?.setImageResource(R.drawable.ic_check)
                    context.copyToClipboard(coin.output.address!!, context.getString(R.string.address))
                    delay(Constants.Time.ITEM_COPY_DELAY.toLong())
                    addressCopyIcon?.setImageResource(R.drawable.ic_copy)
                }
            }

            spend.onClick {
                bottomSheetDialog.cancel()
                onSpendCoin?.invoke(coin)
            }

            bottomSheetDialog.setOnCancelListener {
                removeFromStack(bottomSheetDialog)

                if(!blockDialogRecreate) {
                    onDismiss?.invoke()
                }
            }
            bottomSheetDialog.show()
        }
    }

    override fun onNavigateTo(destination: Int) {
        navigate(destination)
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.utxoDistroFragment
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun actionBarSubtitle(): Int {
        return R.string.report_details_utxo_distro
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
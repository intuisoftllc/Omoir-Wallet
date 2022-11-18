package com.intuisoft.plaid.features.dashboardscreen.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.FragmentWalletSettingsBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WalletSettingsViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.features.settings.ui.SettingsFragment
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.databinding.FragmentSwapBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.SwapViewModel
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.intuisoft.plaid.util.fragmentconfig.ConfigSeedData
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDWallet
import kotlinx.android.synthetic.main.fragment_wallet_settings.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class SwapFragment : PinProtectedFragment<FragmentSwapBinding>() {
    private val viewModel: SwapViewModel by viewModel()
    private val localStore: LocalStoreRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSwapBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())

        return binding.root
    }


    override fun onConfiguration(configuration: FragmentConfiguration?) {

        onBackPressedCallback {
            onNavigateBottomBarSecondaryFragmentBackwards(localStore)
        }

        viewModel.setInitialValues()
        binding.swapPairSend.setOnTextChangedListener {
            viewModel.validateSendAmount(if(it?.isNotEmpty() == true && it.find { Character.isDigit(it) } != null) it.toDouble() else 0.0)
        }

        viewModel.minMax.observe(viewLifecycleOwner, Observer {
            binding.minMaxContainer.isVisible = it != null
            binding.minMaxTitle.isVisible = it != null

            it?.let {
                binding.min.text = it.first
                binding.max.text = it.second
            }
        })

        viewModel.sendPairInfo.observe(viewLifecycleOwner, Observer {
            if(it.ticker.lowercase() == "btc") {
                binding.swapPairSend.setTickerSymbol(R.drawable.ic_bitcoin)
            } else if(it.symbol != null)
                binding.swapPairSend.setTickerSymbol(it.symbol)
            else binding.swapPairSend.setTickerSymbol(0)

            binding.swapPairSend.setPairTitle(it.pairSendReciveTitle)
            binding.swapPairSend.setStyle(it.pairType)
            binding.swapPairSend.setTicker(it.ticker)
            binding.swapPairSend.setValue(it.receiveValue)
        })

        binding.swapSendReceive.setOnClickListener {
            viewModel.swapSendReceive()
        }

        viewModel.recievePairInfo.observe(viewLifecycleOwner, Observer {
            if(it.ticker.lowercase() == "btc") {
                binding.swapPairReceive.setTickerSymbol(R.drawable.ic_bitcoin)
            } else if(it.symbol != null)
                binding.swapPairReceive.setTickerSymbol(it.symbol)
            else binding.swapPairReceive.setTickerSymbol(0)

            binding.swapPairReceive.setPairTitle(it.pairSendReciveTitle)
            binding.swapPairReceive.setStyle(it.pairType)
            binding.swapPairReceive.setTicker(it.ticker)
            binding.swapPairReceive.setValue(it.receiveValue)
        })

        binding.fixed.onClick {
            viewModel.setFixed(true)
            binding.fixed.setButtonStyle(RoundedButtonView.ButtonStyle.ROUNDED_STYLE)
            binding.floating.setButtonStyle(RoundedButtonView.ButtonStyle.OUTLINED_STYLE)
        }

        binding.floating.onClick {
            viewModel.setFixed(false)
            binding.fixed.setButtonStyle(RoundedButtonView.ButtonStyle.OUTLINED_STYLE)
            binding.floating.setButtonStyle(RoundedButtonView.ButtonStyle.ROUNDED_STYLE)
        }

        viewModel.sendReceiveSwapEnabled.observe(viewLifecycleOwner, Observer {
            binding.swapSendReceive.isClickable = it
        })

        viewModel.conversionAmount.observe(viewLifecycleOwner, Observer {
            binding.swapPairReceive.setValue(it)
        })

    }


    override fun onNavigateTo(destination: Int) {
        navigate(destination, viewModel.getWalletId())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun actionBarActionRight(): Int {
        return R.drawable.ic_clock
    }

    override fun onActionRight() {

    }

    override fun actionBarSubtitle(): Int {
        return R.string.exchange
    }

    override fun navigationId(): Int {
        return R.id.swapFragment
    }
}
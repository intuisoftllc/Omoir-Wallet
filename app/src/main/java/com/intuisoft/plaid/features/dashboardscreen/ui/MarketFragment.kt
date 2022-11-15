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
import com.intuisoft.plaid.databinding.FragmentMarketBinding
import com.intuisoft.plaid.databinding.FragmentSwapBinding
import com.intuisoft.plaid.features.dashboardscreen.adapters.BasicLineChartAdapter
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.intuisoft.plaid.util.fragmentconfig.ConfigSeedData
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDWallet
import kotlinx.android.synthetic.main.fragment_wallet_settings.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.random.Random
import kotlin.random.nextLong


class MarketFragment : PinProtectedFragment<FragmentMarketBinding>() {
    private val viewModel: WalletSettingsViewModel by viewModel()
    private val appSettingsViewModel: SettingsViewModel by sharedViewModel()
    private val localStore: LocalStoreRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMarketBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())

        return binding.root
    }


    override fun onConfiguration(configuration: FragmentConfiguration?) {
        onBackPressedCallback {
            onNavigateBottomBarSecondaryFragmentBackwards(localStore)
        }

        val adapter = BasicLineChartAdapter()
        binding.sparkview.setAdapter(adapter)
        binding.sparkview.isScrubEnabled = true
        binding.sparkview.setScrubListener {
            binding.price.text = "${it?.toString()}"
        }

        val series = mutableListOf<Float>()

        var x = 0
        while(x < 150) {
            x++
            var f = x - 30L
            if(f < 0) f = 0
            series.add(Random.nextLong((f)..(x )).toFloat())
        }

        adapter.setItems(series.toFloatArray())
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

    override fun onActionLeft() {
    }

    override fun actionBarSubtitle(): Int {
        return R.string.market_fragment_label
    }

    override fun navigationId(): Int {
        return R.id.marketFragment
    }
}
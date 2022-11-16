package com.intuisoft.plaid.features.dashboardscreen.ui

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
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
import com.intuisoft.plaid.common.model.CongestionRating
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
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.MarketViewModel
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
    private val viewModel: MarketViewModel by viewModel()
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
        while(x < 270) {
            x++
            var f = x - 30L
            if(f < 0) f = 0
            series.add(Random.nextLong((f)..(x )).toFloat())
        }

        adapter.setItems(series.toFloatArray())
        viewModel.maxSupply.observe(viewLifecycleOwner, Observer {
            binding.maxSupply.text = it
        })

        viewModel.circulatingSupply.observe(viewLifecycleOwner, Observer {
            binding.circulatingSupply.text = it
        })

        viewModel.marketCap.observe(viewLifecycleOwner, Observer {
            binding.marketCap.text = it
        })

        if(!localStore.isProEnabled()) {
            binding.height.text = ""
            binding.difficulty.text = ""
            binding.blockchainSize.text = ""
            binding.avgTxSize.text = ""
            binding.avgFeeRate.text = ""
            binding.unconfirmedTxs.text = ""
            binding.avgConfTime.text = ""
        }

        viewModel.congestionRating.observe(viewLifecycleOwner, Observer {
            when(it) {
                CongestionRating.NA -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.text_grey))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_6)
                }
                CongestionRating.LIGHT -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.success_color))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_1)
                }
                CongestionRating.NORMAL -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.text_grey))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_2)
                }
                CongestionRating.MED -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.warning_color))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_3)
                }
                CongestionRating.BUSY -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.warning_color))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_4)
                }
                CongestionRating.CONGESTED -> {
                    binding.congestionRating.setTextColor(resources.getColor(R.color.error_color))
                    binding.congestionRating.text = getString(R.string.market_data_congestion_rating_5)
                }
            }
        })

        binding.bitcoinDescription.setOnClickListener {
            openLink(getString(R.string.market_data_what_is_bitcoin_link))
        }

        binding.bitcoinOrg.setOnClickListener {
            openLink(getString(R.string.market_data_bitcoin_org_link))
        }

        binding.explorer.setOnClickListener {
            openLink(getString(R.string.market_data_bitcoin_explorer_link))
        }

        binding.marketData.setOnClickListener {
            openLink(getString(R.string.market_data_bitcoin_market_external_link))
        }

        viewModel.couldNotLoadData.observe(viewLifecycleOwner, Observer {

        })

        viewModel.network.observe(viewLifecycleOwner, Observer {
            binding.network.text = it
        })

        viewModel.blockHeight.observe(viewLifecycleOwner, Observer {
            binding.height.text = it
        })

        viewModel.difficulty.observe(viewLifecycleOwner, Observer {
            binding.difficulty.text = it
        })

        viewModel.blockchainSize.observe(viewLifecycleOwner, Observer {
            binding.blockchainSize.text = it
        })

        viewModel.avgTxSize.observe(viewLifecycleOwner, Observer {
            binding.avgTxSize.text = it
        })

        viewModel.avgFeeRate.observe(viewLifecycleOwner, Observer {
            binding.avgFeeRate.text = it
        })

        viewModel.unconfirmedTxs.observe(viewLifecycleOwner, Observer {
            binding.unconfirmedTxs.text = it
        })

        viewModel.avgConfTime.observe(viewLifecycleOwner, Observer {
            binding.avgConfTime.text = it
        })

        viewModel.hideMainChainDataContainer.observe(viewLifecycleOwner, Observer {
            binding.mainChainExtendedDataContainer.isVisible = false
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateBasicMarketData()
        viewModel.updateExtendedMarketData()
    }

    fun openLink(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("$url"))
        startActivity(browserIntent)
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
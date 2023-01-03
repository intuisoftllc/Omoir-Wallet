package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.model.ExchangeHistoryFilter
import com.intuisoft.plaid.common.model.ExchangeInfoDataModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.FragmentExchangeHistoryBinding
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.ExchangeHistoryViewModel
import com.intuisoft.plaid.features.homescreen.adapters.ExchangeHistoryAdapter
import com.intuisoft.plaid.util.fragmentconfig.BasicConfigData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExchangeHistoryFragment : ConfigurableFragment<FragmentExchangeHistoryBinding>(pinProtection = true) {
    protected val viewModel: ExchangeHistoryViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()

    private val adapter = ExchangeHistoryAdapter(
        onExchangeSelected = ::onExchangeSelected
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentExchangeHistoryBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.exchanges.adapter = adapter

        viewModel.historyFilter.observe(viewLifecycleOwner, Observer {
            when(it.first) {
                ExchangeHistoryFilter.ALL -> {
                    binding.all.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                    binding.finished.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    binding.failed.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                }

                ExchangeHistoryFilter.FINISHED -> {
                    binding.all.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    binding.finished.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                    binding.failed.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                }

                ExchangeHistoryFilter.FAILED -> {
                    binding.all.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    binding.finished.setButtonStyle(RoundedButtonView.ButtonStyle.TRANSPARENT_STYLE)
                    binding.failed.setButtonStyle(RoundedButtonView.ButtonStyle.PILL_STYLE)
                }
            }

            adapter.addExchanges(it.second.toArrayList())
        })

        binding.swipeContainer.setOnRefreshListener {
            if(binding.swipeContainer.isRefreshing) {
                viewModel.refreshExchanges()
            }
        }

        viewModel.refreshExchanges()
        viewModel.refreshingExchanges.observe(viewLifecycleOwner, Observer {
            binding.swipeContainer.isRefreshing = it
        })

        binding.all.onClick {
            viewModel.setFilter(ExchangeHistoryFilter.ALL)
        }

        binding.finished.onClick {
            viewModel.setFilter(ExchangeHistoryFilter.FINISHED)
        }

        binding.failed.onClick {
            viewModel.setFilter(ExchangeHistoryFilter.FAILED)
        }
    }

    fun onExchangeSelected(exchange: ExchangeInfoDataModel) {
        var bundle = bundleOf(
            Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                configurationType = FragmentConfigurationType.CONFIGURATION_SWAP_DATA,
                configData = BasicConfigData(
                    payload = Gson().toJson(exchange, ExchangeInfoDataModel::class.java)
                )
            )
        )

        navigate(
            R.id.exchangeDetailsFragment,
            bundle,
            Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
        )
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.exchangeHistoryFragment
    }

    override fun actionBarSubtitle(): Int {
        return R.string.swap_history_fragment_label
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
}
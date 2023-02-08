package com.intuisoft.plaid.features.dashboardflow.pro.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventWalletSettingsExportCsv
import com.intuisoft.plaid.databinding.FragmentExportOptionsBinding
import com.intuisoft.plaid.features.dashboardflow.pro.viewmodel.ExportOptionsViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ExportOptionsFragment : ConfigurableFragment<FragmentExportOptionsBinding>(pinProtection = true, premiumContent = true) {
    protected val viewModel: ExportOptionsViewModel by viewModel()
    protected val eventTracker: EventTracker by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentExportOptionsBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.pdfExport.enableButton(false)
        binding.csvExport.onClick {
            eventTracker.log(EventWalletSettingsExportCsv())
            activateAnimatedLoading(true, getString(R.string.export_data_loading_message))
            viewModel.exportToCsv()
        }

        viewModel.exportFinished.observe(viewLifecycleOwner, Observer {
            activateAnimatedLoading(false, "")
            styledSnackBar(requireView(), getString(R.string.export_data_finished_message, it))
        })
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN_WHITE
    }

    override fun navigationId(): Int {
        return R.id.exportOptionsFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
package com.intuisoft.plaid.features.dashboardscreen.ui

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.google.zxing.BarcodeFormat
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentExportWalletBinding
import com.intuisoft.plaid.databinding.FragmentTransactionDetailsBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WalletExportViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.koin.androidx.viewmodel.ext.android.viewModel


class TransactionDetailsFragment : PinProtectedFragment<FragmentTransactionDetailsBinding>() {
    private val viewModel: WalletExportViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTransactionDetailsBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf()
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showActionBar(): Boolean {
        if(configSet())
            return super.showActionBar()

        return false
    }

    override fun actionBarTitle(): Int {
        if(configSet())
            return super.actionBarTitle()

        return 0
    }

    override fun navigationId(): Int {
        return R.id.transactionDetailsFragment
    }
}
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
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WalletExportViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.koin.androidx.viewmodel.ext.android.viewModel


class WalletExportFragment : PinProtectedFragment<FragmentExportWalletBinding>() {
    private val viewModel: WalletExportViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentExportWalletBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_DISPLAY_QR,
                FragmentConfigurationType.CONFIGURATION_DISPLAY_SHAREABLE_QR
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        configuration?.let {
            val configData = it.configData as ConfigQrDisplayData

            binding.pubKeyTitle.isVisible = configData.qrTitle != null
            binding.pubAddress.text = configData.payload
            showQrCode(configData.payload)

            when(it.configurationType) {

                FragmentConfigurationType.CONFIGURATION_DISPLAY_QR -> {
                    binding.pubKeyTitle.text = configData.qrTitle
                }

                FragmentConfigurationType.CONFIGURATION_DISPLAY_SHAREABLE_QR -> {
                    binding.shareButton.isVisible = true
                }
            }
        }

        viewModel.xpubClickable.observe(viewLifecycleOwner, Observer {
            binding.pubAddress.isClickable = it
        })

        viewModel.xpubData.observe(viewLifecycleOwner, Observer {
            binding.pubAddress.text = it
        })

        viewModel.copyXpub.observe(viewLifecycleOwner, Observer {
            if(it) {
                binding.pubAddress.setTextColor(requireContext().getColor(R.color.success_color))
                requireContext().copyToClipboard((configuration!!.configData as ConfigQrDisplayData).payload, "address")
            } else {
                binding.pubAddress.setTextColor(requireContext().getColor(R.color.alt_black))
            }
        })

        binding.pubAddress.setOnClickListener {
            viewModel.copyXpubToClipboard((configuration!!.configData as ConfigQrDisplayData).payload)
        }

    }

    fun mergeBitmaps(logo: Bitmap?, qrcode: Bitmap): Bitmap? {
        val combined = Bitmap.createBitmap(qrcode.width, qrcode.height, qrcode.config)
        val canvas = Canvas(combined)
        val canvasWidth: Int = canvas.getWidth()
        val canvasHeight: Int = canvas.getHeight()
        canvas.drawBitmap(qrcode, Matrix(), null)
        val resizeLogo = Bitmap.createScaledBitmap(logo!!, canvasWidth / 5, canvasHeight / 5, true)
        val centreX = (canvasWidth - resizeLogo.width).toFloat() / 2
        val centreY = (canvasHeight - resizeLogo.height).toFloat() / 2
        canvas.drawBitmap(resizeLogo, centreX, centreY, null)
        return combined
    }

    fun showQrCode(pubKey: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(pubKey, BarcodeFormat.QR_CODE, dpToPixels(350f).toInt(), dpToPixels(350f).toInt())
            val logo: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            val merge = mergeBitmaps(logo, bitmap)
            binding.qrCode.setImageBitmap(merge)
        } catch (e: Exception) {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showActionBar(): Boolean {
        if(configSet())
            return super.showActionBar()

        return true
    }

    override fun actionBarTitle(): Int {
        if(configSet())
            return super.actionBarTitle()

        return R.string.wallet_export_fragment_label
    }

    override fun navigationId(): Int {
        return R.id.exportWalletFragment
    }
}
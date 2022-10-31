package com.intuisoft.plaid.features.dashboardscreen.ui

import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentWalletExportBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WalletExportViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class WalletExportFragment : PinProtectedFragment<FragmentWalletExportBinding>() {
    private val viewModel: WalletExportViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWalletExportBinding.inflate(inflater, container, false)
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

            binding.close.isVisible = configData.showClose
            binding.pubKeyTitle.isVisible = configData.qrTitle != null
            binding.pubKeyTitle.text = configData.qrTitle
            binding.pubAddress.text = configData.payload
            showQrCode(configData.payload)

            when(it.configurationType) {

                FragmentConfigurationType.CONFIGURATION_DISPLAY_QR -> {
                    // do nothing
                }

                FragmentConfigurationType.CONFIGURATION_DISPLAY_SHAREABLE_QR -> {
                    binding.shareButton.isVisible = true
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
                    binding.pubAddress.setTextColor(requireContext().getColor(R.color.brand_color_accent_3))
                }
            })

            binding.pubAddress.setOnClickListener {
                viewModel.copyXpubToClipboard((configuration!!.configData as ConfigQrDisplayData).payload)
            }

            binding.shareButton.onClick {
                requireActivity().shareText(null, configData.payload)
            }

            binding.close.setOnClickListener {
                findNavController().popBackStack()
            }
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
            val hints: MutableMap<EncodeHintType, Any> = EnumMap<EncodeHintType, Any>(
                EncodeHintType::class.java
            )

            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.MARGIN] = 2

            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.encodeBitmap(
                pubKey,
                BarcodeFormat.QR_CODE,
                resources.dpToPixels(250f).toInt(),
                resources.dpToPixels(250f).toInt(),
                hints
            )

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

    override fun actionBarVariant(): Int {
        if(configSet())
            return super.actionBarVariant()

        return TopBarView.CENTER_ALIGN
    }

    override fun actionBarTitle(): Int {
        if(configSet())
            return super.actionBarTitle()

        return R.string.wallet_export_fragment_label
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun navigationId(): Int {
        return R.id.exportWalletFragment
    }
}
package com.intuisoft.plaid.features.dashboardscreen.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentExportWalletBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WalletSettingsViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class WalletExportFragment : PinProtectedFragment<FragmentExportWalletBinding>() {
    private val viewModel: WalletSettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentExportWalletBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())
        return binding.root
    }


    override fun onConfiguration(configuration: FragmentConfiguration?) {
        val xpub = viewModel.getRecieveAddress()

        showQrCode(viewModel.getMasterPublicKey())
        binding.pubAddress.text = "Recieve: $xpub\n\nmasterKey: ${viewModel.getMasterPublicKey()}"
    }

    fun showQrCode(pubKey: String) {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(pubKey, BarcodeFormat.QR_CODE, 512, 512)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            binding.qrCode.setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showActionBar(): Boolean {
        return true
    }

    override fun actionBarTitle(): Int {
        return R.string.wallet_export_fragment_label
    }

    override fun navigationId(): Int {
        return R.id.exportWalletFragment
    }
}
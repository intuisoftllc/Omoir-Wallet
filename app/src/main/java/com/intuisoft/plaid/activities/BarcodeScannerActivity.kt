package com.intuisoft.plaid.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.intuisoft.plaid.androidwrappers.BindingActivity
import com.intuisoft.plaid.androidwrappers.checkAppPermission
import com.intuisoft.plaid.databinding.ActivityScanBarcodeBinding
import com.intuisoft.plaid.util.Constants.ActivityResult.BARCODE_EXTRA
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent


class BarcodeScannerActivity : BindingActivity<ActivityScanBarcodeBinding>(), KoinComponent {


    private lateinit var barcodeDetector: BarcodeDetector
    private lateinit var cameraSource: CameraSource
    private val walletManager: AbstractWalletManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityScanBarcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initialiseDetectorsAndSources() {
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(this, barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()
        binding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    checkAppPermission(Manifest.permission.CAMERA, 100) {
                        cameraSource.start(binding.surfaceView.holder)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode?> {
            override fun release() {
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode?>) {
                val barcodes: SparseArray<Barcode?> = detections.getDetectedItems()

                try {
                    val address = barcodes.valueAt(0)?.rawValue

                    if (barcodes.size() != 0 && walletManager.validAddress(address ?: "")) {
                        closeActivity(address!!)
                    }
                } catch (e: Exception) {}
            }
        })

        binding.cancel.onClick {
            finish()
        }
    }

    fun closeActivity(address: String) {
        val data = Intent()
        data.putExtra(BARCODE_EXTRA, address)
        setResult(RESULT_OK, data)
        finish();
    }

    override fun onPause() {
        super.onPause()
        cameraSource.release()
    }

    override fun onResume() {
        super.onResume()
        initialiseDetectorsAndSources()
    }
}
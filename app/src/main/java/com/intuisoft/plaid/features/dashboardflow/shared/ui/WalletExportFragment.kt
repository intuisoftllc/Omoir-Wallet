package com.intuisoft.plaid.features.dashboardflow.shared.ui

import android.graphics.*
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventDepositBuildInvoice
import com.intuisoft.plaid.common.analytics.events.EventDepositCreateInvoice
import com.intuisoft.plaid.common.analytics.events.EventDepositShareAddress
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.RateConverter
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
import com.intuisoft.plaid.common.util.extensions.charsAfter
import com.intuisoft.plaid.common.util.extensions.containsNumbers
import com.intuisoft.plaid.common.util.extensions.deleteAt
import com.intuisoft.plaid.databinding.FragmentWalletExportBinding
import com.intuisoft.plaid.features.dashboardflow.shared.viewModel.WalletExportViewModel
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import io.horizontalsystems.bitcoincore.models.BitcoinPaymentData
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class WalletExportFragment : ConfigurableFragment<FragmentWalletExportBinding>(pinProtection = true) {
    private val viewModel: WalletExportViewModel by viewModel()
    private val localStoreRepository: LocalStoreRepository by inject()
    protected val eventTracker: EventTracker by inject()

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

            binding.close.visibility = if(configData.showClose) View.VISIBLE else View.INVISIBLE
            binding.invoice.visibility = if(configData.showClose) View.VISIBLE else View.INVISIBLE
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

            binding.invoice.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
                createInvoice()
            }

            viewModel.showInvoice.observe(viewLifecycleOwner, Observer {
                eventTracker.log(EventDepositShareAddress())
                val rateConverter = RateConverter(localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0 )

                rateConverter.setLocalRate(RateConverter.RateType.BTC_RATE, it.amount!!)
                binding.pubKeyTitle.text = rateConverter.from(localStoreRepository.getBitcoinDisplayUnit().toRateType(), localStoreRepository.getLocalCurrency(), false).second
                binding.invoiceDescription.isVisible = true
                binding.invoiceDescription.text = it.label!!
                binding.pubAddress.text = BitcoinPaymentData.toBitcoinPaymentUri(it)
                showQrCode(BitcoinPaymentData.toBitcoinPaymentUri(it))
            })

            viewModel.copyXpub.observe(viewLifecycleOwner, Observer {
                if(it) {
                    binding.pubAddress.setTextColor(requireContext().getColor(R.color.success_color))
                    requireContext().copyToClipboard((configuration!!.configData as ConfigQrDisplayData).payload, "address")
                } else {
                    binding.pubAddress.setTextColor(requireContext().getColor(R.color.brand_color_accent_3))
                }
            })

            binding.pubAddress.setOnSingleClickListener {
                viewModel.copyXpubToClipboard(binding.pubAddress.text.toString())
            }

            binding.shareButton.onClick {
                requireActivity().shareText(null, binding.pubAddress.text.toString())
            }

            binding.close.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
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
                hints,
                requireContext().getColor(R.color.qr_code_whitespace_color),
                requireContext().getColor(R.color.qr_code_data_color)
            )

            val logo: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            val merge = mergeBitmaps(logo, bitmap!!)
            binding.qrCode.setImageBitmap(merge)
        } catch (e: Exception) {
        }
    }

    private fun showInvoiceDisplayType(invoiceAmount: TextView) {
        when(localStoreRepository.getBitcoinDisplayUnit()) {
            BitcoinDisplayUnit.BTC -> {
                invoiceAmount.text = getString(R.string.btc)
            }

            BitcoinDisplayUnit.FIAT -> {
                invoiceAmount.text = SimpleCurrencyFormat.getSymbol(localStoreRepository.getLocalCurrency())
            }

            BitcoinDisplayUnit.SATS -> {
                invoiceAmount.text = getString(R.string.sats)
            }
        }
    }

    fun createInvoice() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        addToStack(bottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_create_invoice)
        val invoiceAmount = bottomSheetDialog.findViewById<EditText>(R.id.invoice_amount)!!
        val conversionType = bottomSheetDialog.findViewById<TextView>(R.id.conversion_type)!!
        val description = bottomSheetDialog.findViewById<EditText>(R.id.description)!!
        val save = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.save)!!
        val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
        val rateConverter = RateConverter(localStoreRepository.getRateFor(localStoreRepository.getLocalCurrency())?.currentPrice ?: 0.0 )
        var watcher: TextWatcher? = null

        eventTracker.log(EventDepositBuildInvoice())
        save.enableButton(false)
        showInvoiceDisplayType(conversionType)
        watcher = invoiceAmount.doOnTextChanged { text, start, before, count ->
            if(text != null) {
                if(text.isNotEmpty() && text.contains(".")
                    && (
                            localStoreRepository.getBitcoinDisplayUnit() == BitcoinDisplayUnit.SATS
                              || (localStoreRepository.getBitcoinDisplayUnit() == BitcoinDisplayUnit.FIAT && text.toString().charsAfter('.') > 2)
                              || (localStoreRepository.getBitcoinDisplayUnit() == BitcoinDisplayUnit.BTC && text.toString().charsAfter('.') > 8)
                        )
                ) {
                    invoiceAmount.setText(text.toString().deleteAt(start))
                    invoiceAmount.setSelection(invoiceAmount.length())
                } else if (text.isNotEmpty() && text.toString().containsNumbers()) {
                    invoiceAmount.removeTextChangedListener(watcher)
                    when (localStoreRepository.getBitcoinDisplayUnit()) {
                        BitcoinDisplayUnit.BTC -> {
                            rateConverter.setLocalRate(
                                RateConverter.RateType.BTC_RATE,
                                text.toString().replace(",", "").toDouble()
                            )
                        }

                        BitcoinDisplayUnit.FIAT -> {
                            val currentValue = rateConverter.from(
                                RateConverter.RateType.FIAT_RATE,
                                localStoreRepository.getLocalCurrency(),
                                false
                            ).first

                            if(currentValue != text.toString()) {
                                rateConverter.setLocalRate(
                                    RateConverter.RateType.FIAT_RATE,
                                    text.toString().replace(",", "").toDouble()
                                )
                            }
                        }

                        BitcoinDisplayUnit.SATS -> {
                            rateConverter.setLocalRate(
                                RateConverter.RateType.SATOSHI_RATE,
                                text.toString().replace(",", "").toDouble()
                            )

                        }
                    }

                    invoiceAmount.addTextChangedListener(watcher)
                    if(rateConverter.getRawBtcRate() > Constants.Limit.BITCOIN_SUPPLY_CAP) {
                        invoiceAmount.setText(text.toString().deleteAt(start))
                    }

                    invoiceAmount.setSelection(invoiceAmount.length())
                } else {
                    rateConverter.setLocalRate(
                        RateConverter.RateType.SATOSHI_RATE,
                        0.0
                    )
                }
            } else {
                rateConverter.setLocalRate(
                    RateConverter.RateType.SATOSHI_RATE,
                    0.0
                )
            }

            save.enableButton(rateConverter.getRawRate() > 0)
        }

        conversionType.setOnSingleClickListener(Constants.Time.MIN_CLICK_INTERVAL_SHORT) {
            when(localStoreRepository.getBitcoinDisplayUnit()) {
                BitcoinDisplayUnit.BTC -> {
                    localStoreRepository.updateBitcoinDisplayUnit(BitcoinDisplayUnit.SATS)
                    invoiceAmount.setText(
                        rateConverter.from(
                                RateConverter.RateType.SATOSHI_RATE,
                                localStoreRepository.getLocalCurrency(),
                                false
                        ).first
                    )
                }

                BitcoinDisplayUnit.SATS -> {
                    localStoreRepository.updateBitcoinDisplayUnit(BitcoinDisplayUnit.FIAT)
                    invoiceAmount.setText(
                        rateConverter.from(
                            RateConverter.RateType.FIAT_RATE,
                            localStoreRepository.getLocalCurrency(),
                            false
                        ).first
                    )
                }

                BitcoinDisplayUnit.FIAT -> {
                    localStoreRepository.updateBitcoinDisplayUnit(BitcoinDisplayUnit.BTC)
                    invoiceAmount.setText(
                        rateConverter.from(
                            RateConverter.RateType.BTC_RATE,
                            localStoreRepository.getLocalCurrency(),
                            false
                        ).first
                    )
                }
            }

            showInvoiceDisplayType(conversionType)
            invoiceAmount.setSelection(invoiceAmount.length())
        }


        save.onClick {
            viewModel.setInvoice(
                rateConverter.getRawBtcRate(),
                description.text.toString()
            )

            eventTracker.log(EventDepositCreateInvoice())
            bottomSheetDialog.cancel()
        }

        cancel.onClick {
            bottomSheetDialog.cancel()
        }

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
        }
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
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
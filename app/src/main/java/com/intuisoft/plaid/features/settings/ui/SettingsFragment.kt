package com.intuisoft.plaid.features.settings.ui

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.intuisoft.plaid.R
import com.intuisoft.plaid.databinding.FragmentSettingsBinding
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.entensions.validateFingerprint
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SettingsFragment : PinProtectedFragment<FragmentSettingsBinding>() {
    private val viewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bitcoinDisplayUnitSetting.observe(viewLifecycleOwner, Observer {
            when(it) {
                BitcoinDisplayUnit.BTC -> {
                    binding.bitcoinUnitSetting.showSubtitleIcon(R.drawable.ic_bitcoin)
                    binding.bitcoinUnitSetting.setSubTitleText(getString(R.string.bitcoin))
                }

                BitcoinDisplayUnit.SATS -> {
                    binding.bitcoinUnitSetting.showSubtitleIcon(R.drawable.ic_satoshi)
                    binding.bitcoinUnitSetting.setSubTitleText(getString(R.string.satoshi))
                }

                else -> {
                    binding.bitcoinUnitSetting.showSubtitleIcon(R.drawable.ic_bitcoin)
                    binding.bitcoinUnitSetting.setSubTitleText(getString(R.string.bitcoin))
                }
            }
        })

        binding.bitcoinUnitSetting.onClick {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToDisplayUnitFragment(),
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        binding.appearanceSetting.onClick {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToAppearanceFragment(),
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        binding.updatePinSetting.onClick {
            val bundle = bundleOf("setupPin" to true)

            findNavController().navigate(
                R.id.pinFragment,
                bundle
            )
        }

        binding.maxPinSettings.onClick {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(R.layout.max_pin_attempts_bottom_sheet)
            val numberPicker = bottomSheetDialog.findViewById<NumberPicker>(R.id.numberPicker)
            val originalLimit = viewModel.getMaxPinAttempts()
            numberPicker?.minValue = 0
            numberPicker?.maxValue = 999
            numberPicker?.value = originalLimit
            numberPicker?.wrapSelectorWheel = true
            numberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
                viewModel.saveMaxPinAttempts(newVal)
            }

            bottomSheetDialog.setOnCancelListener {
                if(viewModel.getMaxPinAttempts() <= Constants.Limit.MIN_RECOMMENDED_PIN_ATTEMPTS) {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(R.string.low_pin_limit_title)
                        .setMessage(R.string.low_pin_limit_risk_message)
                        .setPositiveButton(R.string.risk_acknowledgement_1) { dialog, id ->
                            // do nothing
                        }
                        .setNegativeButton(R.string.cancel) { dialog, id ->
                            viewModel.saveMaxPinAttempts(originalLimit)
                        }

                    val dialog = builder.create()
                    dialog.setOnShowListener {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(requireContext().getColor(R.color.error_color))
                    }

                    dialog.show()
                }
            }

            bottomSheetDialog.show()
        }

        binding.timeoutSetting.onClick {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(R.layout.max_pin_attempts_bottom_sheet)
            val numberPicker = bottomSheetDialog.findViewById<NumberPicker>(R.id.numberPicker)

            numberPicker?.minValue = 0
            numberPicker?.maxValue = 4
            numberPicker?.value = 4
            numberPicker?.displayedValues = arrayOf(
                Constants.Strings.STRING_IMMEDIATE_TIMEOUT,
                Constants.Strings.STRING_1_MINUTE_TIMEOUT,
                Constants.Strings.STRING_2_MINUTE_TIMEOUT,
                Constants.Strings.STRING_5_MINUTE_TIMEOUT,
                Constants.Strings.STRING_10_MINUTE_TIMEOUT
            )

            when(viewModel.getPinTimeout()) {
                Constants.Time.ONE_MINUTE -> {
                    numberPicker?.value = 1
                }
                Constants.Time.TWO_MINUTES -> {
                    numberPicker?.value = 2
                }
                Constants.Time.FIVE_MINUTES -> {
                    numberPicker?.value = 3
                }
                Constants.Time.TEN_MINUTES -> {
                    numberPicker?.value = 4
                }
                else -> {
                    numberPicker?.value = 0
                }
            }

            numberPicker?.wrapSelectorWheel = true
            numberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
                when(newVal) {
                    1 -> {
                        viewModel.savePinTimeout(Constants.Time.ONE_MINUTE)
                    }
                    2 -> {
                        viewModel.savePinTimeout(Constants.Time.TWO_MINUTES)
                    }
                    3 -> {
                        viewModel.savePinTimeout(Constants.Time.FIVE_MINUTES)
                    }
                    4 -> {
                        viewModel.savePinTimeout(Constants.Time.TEN_MINUTES)
                    }
                    else -> {
                        viewModel.savePinTimeout(Constants.Time.INSTANT)
                    }
                }
            }

            bottomSheetDialog.show()
        }

        viewModel.pinTimeoutSetting.observe(viewLifecycleOwner, Observer {
            binding.timeoutSetting.setSubTitleText(viewModel.pinTimeoutToString(it))
        })

        viewModel.validateOrRegisterFingerprintSupport(
            onCheck = { supported ->
                binding.enableFingerprintSetting.disableView(!supported)

                if(supported) {
                    binding.enableFingerprintSetting.setLayoutClickTriggersSwitch()
                }
            },

            onEnroll = {
                binding.enableFingerprintSetting.setLayoutClickTriggersSwitch()
                viewModel.saveFingerprintRegistered(false)
            }
        )

        viewModel.fingerprintRegistered.observe(viewLifecycleOwner, Observer {
            binding.enableFingerprintSetting.setSwitchChecked(it)
        })

        binding.enableFingerprintSetting.onSwitchClicked {
            when {
                !viewModel.isFingerprintEnabled() && it -> {
                    binding.enableFingerprintSetting.setSwitchChecked(false)

                    viewModel.validateOrRegisterFingerprintSupport(
                        onCheck = { supported ->
                            if(supported) {
                                validateFingerprint {
                                    viewModel.saveFingerprintRegistered(true)
                                    binding.enableFingerprintSetting.setSwitchChecked(true)
                                }
                            }
                        },

                        onEnroll = {
                            viewModel.navigateToFingerprintSettings(requireActivity())
                        }
                    )
                }

                viewModel.isFingerprintEnabled() && !it -> {
                    binding.enableFingerprintSetting.setSwitchChecked(true)

                    validateFingerprint(
                        title = Constants.Strings.DISABLE_BIOMETRIC_AUTH,
                        subTitle = Constants.Strings.USE_BIOMETRIC_REASON_4,
                        onSuccess = {
                            viewModel.saveFingerprintRegistered(false)
                            binding.enableFingerprintSetting.setSwitchChecked(false)
                        }
                    )
                }

                else -> {
                    viewModel.saveFingerprintRegistered(it)
                }
            }
        }

        binding.wipeDataSetting.onClick {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.wipe_data_title)
                .setMessage(R.string.wipe_data_message)
                .setPositiveButton(R.string.erase_data) { dialog, id ->

                    if(viewModel.isFingerprintEnabled()) {
                        validateFingerprint(
                            title = Constants.Strings.SCAN_TO_ERASE_DATA,
                            subTitle = Constants.Strings.USE_BIOMETRIC_REASON_3,
                            onSuccess = {
                                wipeData()
                            }
                        )
                    } else {
                        wipeData()
                    }


                }
                .setNegativeButton(R.string.cancel) { dialog, id ->
                    // do nothing
                }

            val dialog = builder.create()
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(requireContext().getColor(R.color.error_color))
            }

            dialog.show()
        }

        viewModel.appVersionSetting.observe(viewLifecycleOwner, Observer {
            binding.appVersionSetting.setSubTitleText(it)
        })

        binding.helpSetting.onClick {
            val emailIntent = Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.business_email)))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_request_subject))

            val release = Build.VERSION.RELEASE
            val sdkVersion = Build.VERSION.SDK_INT
            emailIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.support_request_message,
                    Build.BRAND,
                    "Android SDK: $sdkVersion ($release)",
                    System.getProperty("os.version"),
                    Build.MODEL,
                    Build.PRODUCT
                )
            );


            emailIntent.setType("message/rfc822");

            try {
                startActivity(
                    Intent.createChooser(emailIntent, "Send email using..."));
            } catch (ex: ActivityNotFoundException) {
                Snackbar.make(
                    view,
                    "No email clients installed.",
                    Snackbar.LENGTH_LONG
                ).show()
            }

        }

        binding.aboutUsSetting.onClick {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToAboutUsFragment(),
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }
    }

    fun wipeData() {
        viewModel.eraseAllData()
        findNavController().navigate(
            SettingsFragmentDirections.actionGlobalSplashFragment(),
            navOptions {
                popUpTo(R.id.pinFragment) {
                    inclusive = true
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateSettingsScreen()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showActionBar(): Boolean {
        return true
    }

    override fun actionBarTitle(): Int {
        return R.string.settings_fragment_label
    }
}
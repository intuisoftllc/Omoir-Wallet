package com.intuisoft.plaid.features.settings.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.model.AppTheme
import com.intuisoft.plaid.databinding.FragmentSettingsBinding
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.common.model.BitcoinDisplayUnit
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.SimpleCurrencyFormat
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

    override fun onConfiguration(configuration: FragmentConfiguration?) {

        onBackPressedCallback {
            onNavigateBack()
        }

        viewModel.bitcoinDisplayUnitSetting.observe(viewLifecycleOwner, Observer {
            when(it) {
                BitcoinDisplayUnit.BTC -> {
                    binding.bitcoinUnit.showSubtitleIcon(R.drawable.ic_bitcoin)
                    binding.bitcoinUnit.setSubTitleText(getString(R.string.btc))
                }

                BitcoinDisplayUnit.SATS -> {
                    binding.bitcoinUnit.showSubtitleIcon(R.drawable.ic_satoshi)
                    binding.bitcoinUnit.setSubTitleText(getString(R.string.sats))
                }

                BitcoinDisplayUnit.FIAT -> {
                    if(viewModel.versionTapLimitReached()) {
                        binding.bitcoinUnit.showSubtitleIcon(R.drawable.ic_poo)
                    } else {
                        binding.bitcoinUnit.showSubtitleIcon(R.drawable.ic_fiat)
                    }
                    
                    binding.bitcoinUnit.setSubTitleText(getString(R.string.fiat))
                }

                else -> {
                    binding.bitcoinUnit.showSubtitleIcon(R.drawable.ic_bitcoin)
                    binding.bitcoinUnit.setSubTitleText(getString(R.string.bitcoin))
                }
            }
        })

        viewModel.nameSetting.observe(viewLifecycleOwner, Observer {
            binding.changeName.setSubTitleText(it)
        })

        viewModel.appThemeSetting.observe(viewLifecycleOwner, Observer {
            when(it) {
                AppTheme.LIGHT -> {
                    binding.appearance.setSubTitleText(getString(R.string.settings_option_appearance_light))
                }
                AppTheme.DARK -> {
                    binding.appearance.setSubTitleText(getString(R.string.settings_option_appearance_dark))
                }
                AppTheme.AUTO -> {
                    binding.appearance.setSubTitleText(getString(R.string.settings_option_appearance_auto))
                }
            }
        })

        binding.bitcoinUnit.onClick {
            navigate(
                R.id.displayUnitFragment,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        binding.changeName.onClick {
            changeNameBottomSheet()
        }

        binding.appearance.onClick {
            navigate(
                R.id.appearanceFragment,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        binding.localCurrency.onClick {
            navigate(
                R.id.localCurrencyFragment,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        viewModel.localCurrencySetting.observe(viewLifecycleOwner, Observer {
            binding.localCurrency.setSubTitleText(SimpleCurrencyFormat.formatTypeBasic(it))
        })

        binding.updatePin.onClick {
            navigate(
                R.id.pinFragment,
                bundleOf(Constants.Navigation.PIN_SETUP to true)
            )
        }

        binding.maxAttempts.onClick {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_max_attempts)
            val numberPicker = bottomSheetDialog.findViewById<NumberPicker>(R.id.numberPicker)
            val originalLimit = viewModel.getMaxPinAttempts()
            var newLimit = originalLimit

            numberPicker?.minValue = 1
            numberPicker?.maxValue = 999
            numberPicker?.value = originalLimit
            numberPicker?.wrapSelectorWheel = true
            numberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
                newLimit = newVal
            }

            bottomSheetDialog.setOnCancelListener {
                if(newLimit != originalLimit) {
                    if (newLimit <= Constants.Limit.MIN_RECOMMENDED_PIN_ATTEMPTS) {
                        warningDialog(
                            context = requireContext(),
                            title = getString(R.string.low_pin_entry_title),
                            subtitle = getString(R.string.low_pin_entry_subtitle),
                            positive = getString(R.string.low_pin_entry_positive_button),
                            negative = getString(R.string.cancel),
                            positiveTint = 0,
                            onPositive = {
                                viewModel.saveMaxPinAttempts(newLimit)
                            },
                            onNegative = {
                                viewModel.saveMaxPinAttempts(originalLimit)
                            }
                        )
                    } else {
                        viewModel.saveMaxPinAttempts(newLimit)
                    }
                }
            }

            bottomSheetDialog.show()
        }

        binding.pinTimeout.onClick {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_max_attempts)
            val numberPicker = bottomSheetDialog.findViewById<NumberPicker>(R.id.numberPicker)
            val title = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)

            title?.text = getString(R.string.pin_timeout_title)
            numberPicker?.minValue = 0
            numberPicker?.maxValue = 4
            numberPicker?.displayedValues = arrayOf(
                getString(R.string.settings_option_max_pin_timeout_variant_1),
                getString(R.string.settings_option_max_pin_timeout_variant_2),
                getString(R.string.settings_option_max_pin_timeout_variant_3),
                getString(R.string.settings_option_max_pin_timeout_variant_4),
                getString(R.string.settings_option_max_pin_timeout_variant_5)
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
            binding.pinTimeout.setSubTitleText(viewModel.pinTimeoutToString(requireContext(), it))
        })

        viewModel.validateOrRegisterFingerprintSupport(
            onCheck = { supported ->
                binding.fingerprint.disableView(!supported)

                if(supported) {
                    binding.fingerprint.setLayoutClickTriggersSwitch()
                }
            },

            onEnroll = {
                binding.fingerprint.setLayoutClickTriggersSwitch()
                viewModel.saveFingerprintRegistered(false)
            }
        )

        viewModel.fingerprintRegistered.observe(viewLifecycleOwner, Observer {
            binding.fingerprint.setSwitchChecked(it)
        })

        binding.fingerprint.onSwitchClicked {
            when {
                !viewModel.isFingerprintEnabled() && it -> {
                    binding.fingerprint.setSwitchChecked(false)

                    viewModel.validateOrRegisterFingerprintSupport(
                        onCheck = { supported ->
                            if(supported) {
                                validateFingerprint {
                                    viewModel.saveFingerprintRegistered(true)
                                    binding.fingerprint.setSwitchChecked(true)
                                }
                            }
                        },

                        onEnroll = {
                            viewModel.navigateToFingerprintSettings(requireActivity())
                        }
                    )
                }

                viewModel.isFingerprintEnabled() && !it -> {
                    binding.fingerprint.setSwitchChecked(true)

                    validateFingerprint(
                        title = Constants.Strings.DISABLE_BIOMETRIC_AUTH,
                        subTitle = Constants.Strings.USE_BIOMETRIC_REASON_4,
                        onSuccess = {
                            viewModel.saveFingerprintRegistered(false)
                            binding.fingerprint.setSwitchChecked(false)
                        }
                    )
                }

                else -> {
                    viewModel.saveFingerprintRegistered(it)
                }
            }
        }

        binding.viewWallets.onClick {
            navigate(
                R.id.viewWalletsFragment,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        binding.addressBook.onClick {
            navigate(
                R.id.addressBookFragment,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        binding.minimumConfirmations.onClick {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_max_attempts)
            val title = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
            val numberPicker = bottomSheetDialog.findViewById<NumberPicker>(R.id.numberPicker)

            val originalConfirmations = viewModel.getMinConfirmations()
            title?.setText(getString(R.string.settings_option_view_address_min_confirmations))
            numberPicker?.minValue = 1
            numberPicker?.maxValue = 6
            numberPicker?.value = viewModel.getMinConfirmations()
            numberPicker?.wrapSelectorWheel = true
            numberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
                viewModel.saveMinimumConfirmation(newVal)
            }

            bottomSheetDialog.setOnCancelListener {
                if(originalConfirmations != viewModel.getMinConfirmations()) {
                    viewModel.appRestartNeeded = true
                }
            }

            bottomSheetDialog.show()
        }

        binding.wipeData.onClick {
            warningDialog(
                context = requireContext(),
                title = getString(R.string.settings_option_wipe_data_title),
                subtitle = getString(R.string.settings_option_wipe_data_subtitle),
                positive = getString(R.string.settings_option_wipe_data_button),
                negative = getString(R.string.cancel),
                positiveTint = 0,
                onPositive = {
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
                },
                onNegative = null
            )
        }

        viewModel.appVersionSetting.observe(viewLifecycleOwner, Observer {
            binding.appVersion.setSubTitleText(it)
        })

        viewModel.showEasterEgg.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), "You have unleashed the memes!") {
                navigate(
                    R.id.memeFragment,
                    Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
                )
            }
        })

        binding.appVersion.onClick {
            viewModel.onVersionTapped()
        }

        binding.help.onClick {
            val emailIntent = Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.business_info_email)))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.business_info_support_request_subject))

            val release = Build.VERSION.RELEASE
            val sdkVersion = Build.VERSION.SDK_INT
            emailIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.business_info_support_request_message,
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
                styledSnackBar(requireView(), "No email clients installed.")
            }

        }

        binding.credits.onClick {
            navigate(
                R.id.creditsFragment,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        binding.aboutUs.onClick {
            navigate(
                R.id.aboutUsFragment,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }
    }

    fun wipeData() {
        val progressDialog = ProgressDialog.show(requireContext(), getString(R.string.wiping_data_title), getString(R.string.wiping_data_message))
        progressDialog.setCancelable(false)

        viewModel.eraseAllData {
            progressDialog.cancel()

            navigate(
                navId = R.id.splashFragment,
                options = navOptions {
                    popUpTo(R.id.settingsFragment) {
                        inclusive = true
                    }
                }
            )
        }
    }


    fun changeNameBottomSheet() {
        simpleTextFieldDialog(
            activity = requireActivity(),
            title = getString(R.string.change_name_title),
            fieldType = getString(R.string.name),
            fieldHint = getString(R.string.welcome_alias_suggestion),
            initialText = viewModel.getName() ?: "",
            onSave = {
                viewModel.saveName(it)
            }
        )
    }

    fun onNavigateBack() {
        if(viewModel.appRestartNeeded) {
            warningDialog(
                context = requireContext(),
                title = getString(R.string.settings_option_app_restart_title),
                subtitle = getString(R.string.settings_option_app_restart_subtitle),
                positive = getString(R.string.settings_option_app_restart_positive_text),
                negative = null,
                positiveTint = R.color.brand_color_dark_blue,
                onPositive = {
                    viewModel.restartApp(this)
                },
                onNegative = null
            )
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateSettingsScreen()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN
    }

    override fun actionBarTitle(): Int {
        return R.string.settings_fragment_label
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        onNavigateBack()
    }

    override fun navigationId(): Int {
        return R.id.settingsFragment
    }

    companion object {

        fun simpleTextFieldDialog(
            activity: Activity,
            title: String,
            fieldType: String,
            fieldHint: String,
            initialText: String,
            onSave: ((String) -> Unit)?,
            initiallyEnabled: Boolean = true
        ) {
            val bottomSheetDialog = BottomSheetDialog(activity)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_change_name)
            val sheetTitle = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)!!
            val textFieldType = bottomSheetDialog.findViewById<TextView>(R.id.text_field_type_name)!!
            val save = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.save)!!
            val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
            val name = bottomSheetDialog.findViewById<EditText>(R.id.name)!!
            val textLeft = bottomSheetDialog.findViewById<TextView>(R.id.textLeft)!!

            sheetTitle.text = title
            name.setText(initialText)
            name.hint = fieldHint
            textFieldType.text = fieldType
            save.enableButton(initiallyEnabled)
            name.doOnTextChanged { text, start, before, count ->
                textLeft.text = "${text?.length ?: 0}/25"
                save.enableButton(text?.isNotEmpty() == true && text.isNotBlank())
            }

            name.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                    // if the event is a key down event on the enter button
                    if (event.action == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_ENTER
                    ) {
                        activity.hideSoftKeyboard()
                        name.clearFocus()
                        name.isCursorVisible = false

                        return true
                    }
                    return false
                }
            })

            save.onClick {
                onSave?.invoke(name.text.toString())
                bottomSheetDialog.cancel()
            }

            cancel.onClick {
                bottomSheetDialog.cancel()
            }

            bottomSheetDialog.show()
        }

        fun warningDialog(
            context: Context,
            title: String,
            subtitle: String,
            positive: String,
            negative: String?,
            positiveTint: Int,
            onPositive: (() -> Unit)?,
            onNegative: (() -> Unit)?,
            isCancellable: Boolean = false
        ) {
            val bottomSheetDialog = BottomSheetDialog(context)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_warning)
            val sheetTitle = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)!!
            val sheetSubtitle =
                bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_subtitle)!!
            val save = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.save)!!
            val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!

            bottomSheetDialog.setCancelable(isCancellable)
            sheetTitle.text = title
            sheetSubtitle.text = subtitle
            save.setButtonText(positive)
            if(negative != null) {
                cancel.setButtonText(negative)
            } else cancel.isVisible = false
            save.setTint(positiveTint)

            save.onClick {
                bottomSheetDialog.cancel()
                onPositive?.invoke()
            }

            cancel.onClick {
                bottomSheetDialog.cancel()
                onNegative?.invoke()
            }

            bottomSheetDialog.show()
        }
    }
}
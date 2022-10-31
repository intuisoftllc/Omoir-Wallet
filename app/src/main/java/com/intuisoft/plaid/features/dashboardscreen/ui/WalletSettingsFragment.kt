package com.intuisoft.plaid.features.dashboardscreen.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentWalletSettingsBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WalletSettingsViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.features.settings.ui.SettingsFragment
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.intuisoft.plaid.util.fragmentconfig.ConfigSeedData
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDWallet
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class WalletSettingsFragment : PinProtectedFragment<FragmentWalletSettingsBinding>() {
    private val viewModel: WalletSettingsViewModel by viewModel()
    private val appSettingsViewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWalletSettingsBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())

        viewModel.fromSettings = requireArguments().getBoolean(Constants.Navigation.FROM_SETTINGS)
        return binding.root
    }


    override fun onConfiguration(configuration: FragmentConfiguration?) {

        viewModel.updateWalletSettings()
        viewModel.walletName.observe(viewLifecycleOwner, Observer {
            binding.renameWallet.setSubTitleText(it)
        })

        onBackPressedCallback {
            onNavigateBack()
        }

        viewModel.walletBip.observe(viewLifecycleOwner, Observer {
            when(it) {
                HDWallet.Purpose.BIP84 -> {
                    binding.bip.setSubTitleText(getString(R.string.create_wallet_advanced_options_bip_1))
                }
                HDWallet.Purpose.BIP49 -> {
                    binding.bip.setSubTitleText(getString(R.string.create_wallet_advanced_options_bip_2))
                }
                HDWallet.Purpose.BIP44 -> {
                    binding.bip.setSubTitleText(getString(R.string.create_wallet_advanced_options_bip_3))
                }
            }
        })

        viewModel.walletNetwork.observe(viewLifecycleOwner, Observer {
            if(it == BitcoinKit.NetworkType.TestNet) {
                binding.network.setSubTitleText(getString(R.string.test_net))
            } else {
                binding.network.setSubTitleText(getString(R.string.main_net))
            }
        })

        binding.syncType.onClick {
            showSyncTypeDialog()
        }

        binding.seedPhrase.onClick {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    actionBarTitle = R.string.seed_phrase_basic_fragment_label,
                    actionBarSubtitle = 0,
                    actionBarVariant = TopBarView.CENTER_ALIGN,
                    actionLeft = R.drawable.ic_arrow_left,
                    actionRight = 0,
                    configurationType = FragmentConfigurationType.CONFIGURATION_BASIC_SEED_SCREEN,
                    configData = ConfigSeedData(
                        seedPhrase = viewModel.getWalletSeedPhrase(),
                        passphrase = viewModel.getWalletPassphrase()
                    )
                )
            )

            navigate(
                R.id.seedPhraseFragment,
                bundle
            )
        }

        binding.passphrase.onClick {
            showPassphraseDialog()
        }

        binding.exportWallet.onClick {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    actionBarTitle = R.string.wallet_export_fragment_label,
                    actionBarSubtitle = 0,
                    actionBarVariant = TopBarView.CENTER_ALIGN,
                    actionLeft = R.drawable.ic_arrow_left,
                    actionRight = 0,
                    configurationType = FragmentConfigurationType.CONFIGURATION_DISPLAY_QR,
                    configData = ConfigQrDisplayData(
                        payload = viewModel.getMasterPublicKey(),
                        qrTitle = getString(R.string.export_wallet_title),
                        showClose = false
                    )
                ),
                Constants.Navigation.WALLET_UUID_BUNDLE_ID to viewModel.getWalletId()
            )

            navigate(
                R.id.exportWalletFragment,
                bundle
            )
        }

        binding.renameWallet.onClick {
            SettingsFragment.simpleTextFieldDialog(
                activity = requireActivity(),
                title = getString(R.string.change_name_title),
                fieldType = getString(R.string.name),
                fieldHint = getString(R.string.name_wallet_hint),
                initialText = viewModel.getWalletName(),
                onSave = {
                    viewModel.updateWalletName(it)
                    viewModel.updateWalletSettings()
                }
            )
        }

        binding.deleteWallet.onClick {
            SettingsFragment.warningDialog(
                context = requireContext(),
                title = getString(R.string.wallet_settings_delete_wallet_title),
                subtitle = getString(R.string.wallet_settings_delete_wallet_subtitle),
                positive = getString(R.string.settings_option_wipe_data_button),
                negative = getString(R.string.cancel),
                positiveTint = 0,
                onPositive = {
                    if(viewModel.isFingerprintEnabled()) {
                        validateFingerprint(
                            title = Constants.Strings.SCAN_TO_ERASE_DATA,
                            subTitle = Constants.Strings.USE_BIOMETRIC_REASON_5,
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
    }

    fun onNavigateBack() {
        if(appSettingsViewModel.appRestartNeeded && !viewModel.fromSettings) {
            SettingsFragment.warningDialog(
                context = requireContext(),
                title = getString(R.string.settings_option_app_restart_title),
                subtitle = getString(R.string.settings_option_app_restart_subtitle),
                positive = getString(R.string.settings_option_app_restart_positive_text),
                negative = null,
                positiveTint = R.color.brand_color_dark_blue,
                onPositive = {
                    appSettingsViewModel.restartApp(this)
                },
                onNegative = null
            )
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showSyncTypeDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_sync_type)
        val coreSync = bottomSheetDialog.findViewById<SettingsItemView>(R.id.coreSync)
        val apiSync = bottomSheetDialog.findViewById<SettingsItemView>(R.id.apiSync)

        coreSync?.checkRadio(!viewModel.hasApiSyncMode())
        apiSync?.checkRadio(viewModel.hasApiSyncMode())

        coreSync?.onClick {
            appSettingsViewModel.appRestartNeeded = true
            viewModel.updateWalletSyncMode(false)
            apiSync?.checkRadio(false)
            coreSync.checkRadio(true)
        }

        apiSync?.onClick {
            appSettingsViewModel.appRestartNeeded = true
            viewModel.updateWalletSyncMode(true)
            apiSync.checkRadio(true)
            coreSync?.checkRadio(false)
        }

        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }

    fun showPassphraseDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_passphrase)
        val save = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.save)!!
        val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
        val textLeft = bottomSheetDialog.findViewById<TextView>(R.id.textLeft)!!
        val passphrase = bottomSheetDialog.findViewById<EditText>(R.id.passphrase)!!
        val confirmPassphrase = bottomSheetDialog.findViewById<EditText>(R.id.confirm_passphrase)!!
        val validationError = bottomSheetDialog.findViewById<TextView>(R.id.validation_error)!!

        val originalPassphrase = viewModel.getWalletPassphrase()
        if(originalPassphrase.isNotEmpty()) {
            passphrase.setText(originalPassphrase)
            save.enableButton(false)
        }
        passphrase.doOnTextChanged { text, start, before, count ->
            textLeft.text = "${text?.length ?: 0}/50"
            save.enableButton((text?.isNotEmpty() ?: false && confirmPassphrase.text.isNotEmpty())
                    || (text?.isEmpty() ?: true && confirmPassphrase.text.isEmpty()))
            validationError.isVisible = false
        }

        confirmPassphrase.doOnTextChanged { text, start, before, count ->
            save.enableButton((text?.isNotEmpty() ?: false && passphrase.text.isNotEmpty())
                    || (text?.isEmpty() ?: true && passphrase.text.isEmpty()))
            validationError.isVisible = false
        }

        passphrase.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // if the event is a key down event on the enter button
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    requireActivity().hideSoftKeyboard()
                    passphrase.clearFocus()
                    passphrase.isCursorVisible = false

                    return true
                }
                return false
            }
        })

        confirmPassphrase.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // if the event is a key down event on the enter button
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    requireActivity().hideSoftKeyboard()
                    confirmPassphrase.clearFocus()
                    confirmPassphrase.isCursorVisible = false

                    return true
                }
                return false
            }
        })

        save.onClick {
            if(passphrase.text.toString() == confirmPassphrase.text.toString()) {
                if(passphrase.text.toString() != originalPassphrase) {
                    appSettingsViewModel.appRestartNeeded = true
                    viewModel.setPassphrase(passphrase.text.toString())
                }

                bottomSheetDialog.cancel()
            } else {
                validationError.text = getString(R.string.wallet_settings_passphrase_error)
                validationError.isVisible = true
            }
        }

        cancel.onClick {
            bottomSheetDialog.cancel()
        }


        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }

    fun wipeData() {
        val progressDialog = ProgressDialog.show(requireContext(), getString(R.string.wallet_settings_deleting_wallet_title), getString(R.string.wallet_settings_deleting_wallet_message))
        progressDialog.setCancelable(false)

        viewModel.deleteWallet {
            progressDialog.cancel()
            navigate(R.id.splashFragment)
        }
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        onNavigateBack()
    }

    override fun actionBarTitle(): Int {
        return R.string.wallet_settings_fragment_label
    }

    override fun navigationId(): Int {
        return R.id.settingsFragment
    }
}
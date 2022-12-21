package com.intuisoft.plaid.features.dashboardflow.shared.ui

import WalletSettingsViewModel
import android.app.ProgressDialog
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
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
import com.intuisoft.plaid.features.settings.ui.SettingsFragment
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.intuisoft.plaid.util.fragmentconfig.ConfigSeedData
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDWallet
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class WalletSettingsFragment : ConfigurableFragment<FragmentWalletSettingsBinding>(pinProtection = true) {
    private val viewModel: WalletSettingsViewModel by viewModel()
    private val appSettingsViewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWalletSettingsBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())

        viewModel.fromSettings = arguments?.getBoolean(Constants.Navigation.FROM_SETTINGS) ?: false
        return binding.root
    }


    override fun onConfiguration(configuration: FragmentConfiguration?) {

        viewModel.updateWalletSettings()
        viewModel.checkReadOnlyStatus()
        viewModel.showHiddenWalletsCount()
        viewModel.walletName.observe(viewLifecycleOwner, Observer {
            binding.renameWallet.setSubTitleText(it)
        })

        viewModel.hiddenWallets.observe(viewLifecycleOwner, Observer {
            binding.hiddenWalletsCount.isVisible = it != null

            it?.let {
                binding.hiddenWalletsCount.text =
                    getString(R.string.wallet_settings_hidden_wallets_count, it.toString())
            }
        })

        onBackPressedCallback {
            onNavigateBack()
        }

        viewModel.walletBip.observe(viewLifecycleOwner, Observer {
            binding.bip.disableView(true)

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
            binding.network.disableView(true)

            if(it == BitcoinKit.NetworkType.TestNet) {
                binding.network.setSubTitleText(getString(R.string.test_net))
            } else {
                binding.network.setSubTitleText(getString(R.string.main_net))
            }
        })

        viewModel.readOnlyWallet.observe(viewLifecycleOwner, Observer {
            binding.passphrase.disableView(true)
            binding.seedPhrase.disableView(true)
        })

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
                )
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
                },
                addToStack = ::addToStack,
                removeFromStack = ::removeFromStack
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
                onNegative = null,
                addToStack = ::addToStack,
                removeFromStack = ::removeFromStack
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
                onNegative = null,
                addToStack = ::addToStack,
                removeFromStack = ::removeFromStack
            )
        } else {
            findNavController().popBackStack()
        }
    }

    fun showPassphraseDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        addToStack(bottomSheetDialog)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_passphrase)
        val save = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.save)!!
        val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
        val textLeft = bottomSheetDialog.findViewById<TextView>(R.id.textLeft)!!
        val passphrase = bottomSheetDialog.findViewById<EditText>(R.id.passphrase)!!
        val confirmPassphrase = bottomSheetDialog.findViewById<EditText>(R.id.confirm_passphrase)!!
        val validationError = bottomSheetDialog.findViewById<TextView>(R.id.validation_error)!!
        val showHide = bottomSheetDialog.findViewById<ImageView>(R.id.showHide)!!

        var showPassphrase = false
        showHide.setImageResource(R.drawable.ic_eye_closed)
        passphrase.transformationMethod = PasswordTransformationMethod.getInstance()
        confirmPassphrase.transformationMethod = PasswordTransformationMethod.getInstance()

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

        showHide.setOnClickListener {
            showPassphrase = !showPassphrase

            if(showPassphrase) {
                showHide.setImageResource(R.drawable.ic_eye_open)
                passphrase.transformationMethod = null
                confirmPassphrase.transformationMethod = null
                passphrase.setSelection(passphrase.length())
                confirmPassphrase.setSelection(confirmPassphrase.length())
            } else {
                showHide.setImageResource(R.drawable.ic_eye_closed)
                passphrase.transformationMethod = PasswordTransformationMethod.getInstance()
                confirmPassphrase.transformationMethod = PasswordTransformationMethod.getInstance()
                passphrase.setSelection(passphrase.length())
                confirmPassphrase.setSelection(confirmPassphrase.length())
            }
        }

        save.onClick {
            if(passphrase.text.toString() == confirmPassphrase.text.toString()) {
                if(viewModel.canCreatePassphrase(passphrase.text.toString())) {
                    if (passphrase.text.toString() != originalPassphrase) {
                        appSettingsViewModel.appRestartNeeded = true
                        viewModel.setPassphrase(passphrase.text.toString())
                    }

                    bottomSheetDialog.cancel()
                } else {
                    validationError.text = getString(R.string.wallet_settings_passphrase_limit_error)
                    validationError.isVisible = true
                }
            } else {
                validationError.text = getString(R.string.wallet_settings_passphrase_error)
                validationError.isVisible = true
            }
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

    fun wipeData() {
        val progressDialog = ProgressDialog.show(requireContext(), getString(R.string.wallet_settings_deleting_wallet_title), getString(R.string.wallet_settings_deleting_wallet_message))
        progressDialog.setCancelable(false)

        viewModel.deleteWallet {
            progressDialog.cancel()
            if(viewModel.fromSettings) {
                appSettingsViewModel.appRestartNeeded = true
                onNavigateBack()
            } else {
                navigate(R.id.splashFragment)
            }
        }
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
        return R.id.walletSettingsFragment
    }
}
package com.intuisoft.plaid.features.dashboardflow.shared.ui

import WalletSettingsViewModel
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.BuildConfig
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.*
import com.intuisoft.plaid.common.model.HiddenWalletModel
import com.intuisoft.plaid.databinding.FragmentWalletSettingsBinding
import com.intuisoft.plaid.features.settings.ui.SettingsFragment
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.mapToListOf
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.SavedAccountsAdapter
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
import com.intuisoft.plaid.util.fragmentconfig.ConfigSeedData
import io.horizontalsystems.bitcoinkit.BitcoinKit
import io.horizontalsystems.hdwalletkit.HDWallet
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.HashMap


class WalletSettingsFragment : ConfigurableFragment<FragmentWalletSettingsBinding>(pinProtection = true) {
    private val viewModel: WalletSettingsViewModel by viewModel()
    private val appSettingsViewModel: SettingsViewModel by sharedViewModel()
    protected val eventTracker: EventTracker by inject()
    protected val billing: BillingManager by inject()

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

        eventTracker.log(EventWalletSettingsView())
        viewModel.updateWalletSettings()
        viewModel.showHiddenWalletsCount()
        viewModel.checkProStatus()
        viewModel.showPrivKeySetting()
        viewModel.enableSeedPhraseSetting()
        viewModel.enableHiddenWallet()

        viewModel.walletName.observe(viewLifecycleOwner, Observer {
            binding.renameWallet.setSubTitleText(it)
        })

        viewModel.seedPhraseSettingEnabled.observe(viewLifecycleOwner, Observer {
            binding.seedPhrase.isVisible = it != null
            binding.seedPhrase.disableView(it == false)
        })

        viewModel.showPrivKeySetting.observe(viewLifecycleOwner, Observer {
            binding.privateKey.isVisible = it
        })

        viewModel.hiddenWalletEnabled.observe(viewLifecycleOwner, Observer {
            binding.hiddenWallet.disableView(!it)
        })

        binding.hiddenWallet.onClick {
            eventTracker.log(EventWalletSettingsViewHiddenWallet())
            showPassphraseDialog(viewModel.getHiddenWallet())
        }

        viewModel.hiddenWallets.observe(viewLifecycleOwner, Observer {
            binding.hiddenWalletsCount.isVisible = it != null

            it?.let {
                binding.hiddenWalletsCount.text =
                    getString(R.string.wallet_settings_hidden_wallets_count, it.toString())
            }
        })

        viewModel.upgradeToPro.observe(viewLifecycleOwner, Observer {
            if(it) {
                binding.exportWalletTx.setTitleText(getString(R.string.wallet_settings_export_tx_data_without_pro))
                binding.exportWalletTx.disableView(true)
            } else {
                binding.exportWalletTx.setTitleText(getString(R.string.wallet_settings_export_tx_data))
            }
        })

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

        binding.privateKey.onClick {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    actionBarTitle = R.string.wallet_settings_prv_key,
                    actionBarSubtitle = 0,
                    actionBarVariant = TopBarView.CENTER_ALIGN,
                    actionLeft = R.drawable.ic_arrow_left,
                    actionRight = 0,
                    configurationType = FragmentConfigurationType.CONFIGURATION_DISPLAY_QR,
                    configData = ConfigQrDisplayData(
                        payload = viewModel.getPrvKey(),
                        qrTitle = getString(R.string.wallet_settings_prv_key_title),
                        showClose = false
                    )
                )
            )

            navigate(
                R.id.exportWalletFragment,
                bundle
            )
        }

        binding.walletHelp.onClick {
            val status = viewModel.getWalletStatus()

            val release = Build.VERSION.RELEASE
            val sdkVersion = Build.VERSION.SDK_INT

            sendEmail(
                to = getString(R.string.business_info_email),
                subject = getString(R.string.business_info_support_request_subject_wallet),
                message = getString(R.string.business_info_support_request_wallet_state_message,
                    viewModel.getWallet()!!.testNetWallet.toString(),
                    viewModel.getWallet()!!.hiddenWallet.toString(),
                    status[Constants.Strings.STATUS_INFO_1],
                    status[Constants.Strings.STATUS_INFO_2],
                    status[Constants.Strings.STATUS_INFO_3],
                    status[Constants.Strings.STATUS_INFO_4],
                    status[Constants.Strings.STATUS_INFO_5],
                    viewModel.getWallet()!!.walletKit!!.getConnectedPeersCount()
                        .toLong().mapToListOf {
                            try {
                                val peerInfo = status["${Constants.Strings.STATUS_INFO_6}${it + 1}"] as HashMap<String, String>

                                """
                                    Peer #${it + 1}
                                    Status: ${peerInfo[Constants.Strings.PEER_STATUS_INFO_1]}
                                    Host: ${peerInfo[Constants.Strings.PEER_STATUS_INFO_2]}
                                    Best Block: ${peerInfo[Constants.Strings.PEER_STATUS_INFO_3]}
                                    Tasks: ${peerInfo[Constants.Strings.PEER_STATUS_INFO_4]}
                                """.trimIndent()
                            } catch(t: Throwable) {
                                ""
                            }

                        }.joinToString("\n~\n"),
                    Build.BRAND,
                    "Android SDK: $sdkVersion ($release)",
                    System.getProperty("os.version"),
                    Build.MODEL,
                    Build.PRODUCT,
                    if(viewModel.isProEnabled()) Constants.Strings.PRO_SUBSCRIPTION_MARK + "\n" else "",
                    billing.getUserId(),
                    BuildConfig.VERSION_NAME
                )
            )
        }

        binding.exportWalletTx.onClick {
            eventTracker.log(EventWalletSettingsExportTransactions())
            navigate(
                R.id.exportOptionsFragment
            )
        }

        viewModel.walletNetwork.observe(viewLifecycleOwner, Observer {
            binding.network.disableView(true)

            if(it == BitcoinKit.NetworkType.TestNet) {
                binding.network.setSubTitleText(getString(R.string.test_net))
            } else {
                binding.network.setSubTitleText(getString(R.string.main_net))
            }
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
                        seedPhrase = viewModel.getWalletSeedPhrase()
                    )
                )
            )

            navigate(
                R.id.seedPhraseFragment,
                bundle
            )
        }

        binding.hiddenWallet.onClick {
            eventTracker.log(EventWalletSettingsViewHiddenWallet())
            showPassphraseDialog(viewModel.getHiddenWallet())
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
                    eventTracker.log(EventWalletSettingsRenameWallet())
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
                                eventTracker.log(EventWalletSettingsDeleteWallet())
                                wipeData()
                            }
                        )
                    } else {
                        eventTracker.log(EventWalletSettingsDeleteWallet())
                        wipeData()
                    }
                },
                onNegative = null,
                addToStack = ::addToStack,
                removeFromStack = ::removeFromStack
            )
        }
    }

    override fun onBackPressed() {
        if(appSettingsViewModel.appRestartNeeded && !viewModel.fromSettings) {
            SettingsFragment.warningDialog(
                context = requireContext(),
                title = getString(R.string.settings_option_app_restart_title),
                subtitle = getString(R.string.settings_option_app_restart_subtitle),
                positive = getString(R.string.settings_option_app_restart_positive_text),
                negative = null,
                positiveTint = R.color.brand_color_dark_blue,
                onPositive = {
                    softRestart()
                },
                onNegative = null,
                addToStack = ::addToStack,
                removeFromStack = ::removeFromStack
            )
        } else {
            findNavController().popBackStack()
        }
    }

    fun showPassphraseDialog(hiddenWallet: HiddenWalletModel?) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        addToStack(bottomSheetDialog) {

        }
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_passphrase)
        val save = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.save)!!
        val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
        val textLeft = bottomSheetDialog.findViewById<TextView>(R.id.textLeft)!!
        val passphrase = bottomSheetDialog.findViewById<EditText>(R.id.hidden_wallet)!!
        val confirmPassphrase = bottomSheetDialog.findViewById<EditText>(R.id.confirm_passphrase)!!
        val validationError = bottomSheetDialog.findViewById<TextView>(R.id.validation_error)!!
        val showHide = bottomSheetDialog.findViewById<ImageView>(R.id.showHide)!!
        val accountNumberSetting = bottomSheetDialog.findViewById<SettingsItemView>(R.id.account_number)!!

        var showPassphrase = false
        var HiddenWallet = hiddenWallet ?: HiddenWalletModel("", "", viewModel.getDefaultAccount())
        showHide.setImageResource(R.drawable.ic_eye_closed)
        passphrase.transformationMethod = PasswordTransformationMethod.getInstance()
        confirmPassphrase.transformationMethod = PasswordTransformationMethod.getInstance()

        val accountNumber = HiddenWallet.account.account
        accountNumberSetting.setSubTitleText("$accountNumber (${HiddenWallet.account.accountName})")

        passphrase.setText(hiddenWallet?.passphrase)
        passphrase.doOnTextChanged { text, start, before, count ->
            textLeft.text = "${text?.length ?: 0}/50"
            HiddenWallet.passphrase = text?.toString() ?: ""
            save.enableButton((text?.isNotEmpty() ?: false && confirmPassphrase.text.isNotEmpty())
                    || (text?.isEmpty() ?: true && confirmPassphrase.text.isEmpty()))
            validationError.isVisible = false
        }

        confirmPassphrase.doOnTextChanged { text, start, before, count ->
            save.enableButton((text?.isNotEmpty() ?: false && passphrase.text.isNotEmpty())
                    || (text?.isEmpty() ?: true && passphrase.text.isEmpty()))
            validationError.isVisible = false
        }

        accountNumberSetting.onClick {
            bottomSheetDialog.cancel()
            if(viewModel.showDerivationPathChangeWarning()) {
                SettingsFragment.warningDialog(
                    context = requireContext(),
                    title = getString(R.string.wallet_settings_derivation_path_change_title),
                    subtitle = getString(R.string.wallet_settings_derivation_path_change_subtitle),
                    positive = getString(R.string.i_understand_the_risks),
                    negative = getString(R.string.cancel),
                    positiveTint = 0,
                    onPositive = {
                        showSavedAccountsBottomSheet(HiddenWallet)
                        viewModel.hideDerivationPathChangeWarning()
                    },
                    onNegative = null,
                    isCancellable = true,
                    addToStack = ::addToStack,
                    removeFromStack = ::removeFromStack
                )
            } else {
                showSavedAccountsBottomSheet(HiddenWallet)
            }
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
            val Passphrase = passphrase.text.toString().trim()
            val ConfirmPassphrase = confirmPassphrase.text.toString().trim()

            if(Passphrase == ConfirmPassphrase) {
                if(viewModel.canCreatePassphrase(Passphrase, HiddenWallet.account)) {
                    viewModel.setHiddenWalletParams(Passphrase, HiddenWallet.account) {
                        eventTracker.log(EventWalletSettingsSetPassphrase())
                        appSettingsViewModel.appRestartNeeded = true
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

    fun showSavedAccountsBottomSheet(hiddenWallet: HiddenWalletModel?) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        var doNotRecreate = false
        addToStack(bottomSheetDialog) {
            doNotRecreate = true
        }
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_saved_accounts)
        val addAccount = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.add_account)!!
        val noAccounts = bottomSheetDialog.findViewById<TextView>(R.id.no_accounts)!!
        val accounts = bottomSheetDialog.findViewById<RecyclerView>(R.id.accounts)!!
        val savedAccounts = viewModel.getSavedAccounts()

        if(savedAccounts.isEmpty()) {
            noAccounts.isVisible = true
            accounts.isVisible = false
        } else {
            val adapter =
                SavedAccountsAdapter {
                    bottomSheetDialog.cancel()
                    doNotRecreate = true
                    showPassphraseDialog(HiddenWalletModel(hiddenWallet?.uuid ?: "", hiddenWallet?.passphrase ?: "", it))
                }

            accounts.adapter = adapter
            adapter.addSavedAccounts(savedAccounts.toArrayList())
        }


        addAccount.onClick {
            doNotRecreate = true
            bottomSheetDialog.cancel()
            showSaveAccountDialog(
                activity = requireActivity(),
                titleText = getString(R.string.saved_account_save_title),
                saveButtonText = getString(R.string.save),
                cancelButtonText = getString(R.string.cancel),
                getDerivaitionPath = {
                    "m/${viewModel.getWalletBip().value}'/0'/$it'"
                },
                accountExists = { name ->
                    viewModel.getSavedAccount(name) != null
                },
                saveAccount = { name, accountNumber ->
                    viewModel.saveAccount(name, accountNumber)
                    showSavedAccountsBottomSheet(hiddenWallet)
                },
                onCancel = {
                   if(!doNotRecreate) {
                       showSavedAccountsBottomSheet(hiddenWallet)
                   }
                },
                addToStack = ::addToStack,
                removeFromStack = ::removeFromStack
            )
        }

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
            if(!doNotRecreate) {
                showPassphraseDialog(hiddenWallet)
            }
        }
        bottomSheetDialog.show()
    }

    fun wipeData() {
        val progressDialog = ProgressDialog.show(requireContext(), getString(R.string.wallet_settings_deleting_wallet_title), getString(R.string.wallet_settings_deleting_wallet_message))
        progressDialog.setCancelable(false)

        viewModel.deleteWallet {
            progressDialog.cancel()
            if(viewModel.fromSettings) {
                appSettingsViewModel.appRestartNeeded = true
                onBackPressed()
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
        onBackPressed()
    }

    override fun actionBarTitle(): Int {
        return R.string.wallet_settings_fragment_label
    }

    override fun navigationId(): Int {
        return R.id.walletSettingsFragment
    }

    companion object {

        fun showSaveAccountDialog(
            activity: Activity,
            titleText: String,
            saveButtonText: String,
            cancelButtonText: String,
            initialNameText: String = "",
            initialAccountText: String = "",
            saveAccount: (String, Int) -> Unit,
            accountExists: (String) -> Boolean,
            getDerivaitionPath: (String) -> String,
            onCancel: (() -> Unit)? = null,
            addToStack: (AppCompatDialog) -> Unit,
            removeFromStack: (AppCompatDialog) -> Unit
        ) {
            val bottomSheetDialog = BottomSheetDialog(activity)
            addToStack(bottomSheetDialog)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_save_account)
            val title = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)!!
            val save = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.save)!!
            val derivationPath = bottomSheetDialog.findViewById<TextView>(R.id.derivation_path)!!
            val cancel = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.cancel)!!
            val name = bottomSheetDialog.findViewById<EditText>(R.id.name)!!
            val textLeft = bottomSheetDialog.findViewById<TextView>(R.id.textLeft)!!
            val account = bottomSheetDialog.findViewById<EditText>(R.id.account)!!
            val errorMessage = bottomSheetDialog.findViewById<TextView>(R.id.validation_error)!!

            title.setText(titleText)
            save.setButtonText(saveButtonText)
            cancel.setButtonText(cancelButtonText)
            name.setText(initialNameText)
            account.setText(initialAccountText)
            derivationPath.setText(getDerivaitionPath(initialAccountText))
            save.enableButton(false)
            name.doOnTextChanged { text, start, before, count ->
                textLeft.text = "${text?.length ?: 0}/25"
                save.enableButton(text?.isNotBlank() ?: false && account.text.isNotBlank())
            }

            account.doOnTextChanged { text, start, before, count ->
                derivationPath.setText(getDerivaitionPath(text?.toString() ?: ""))
                save.enableButton(text?.isNotBlank() ?: false && name.text.isNotBlank())
            }

            account.setOnKeyListener(object : View.OnKeyListener {
                override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                    // if the event is a key down event on the enter button
                    if (event.action == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_ENTER
                    ) {
                        activity.hideSoftKeyboard()
                        account.clearFocus()
                        account.isCursorVisible = false

                        return true
                    }
                    return false
                }
            })

            save.onClick {
                val accountNumber =
                    try {
                        account.text.toString().toLong()
                    } catch (e: Throwable) {
                        0L
                    }

                if(accountNumber <= Int.MAX_VALUE) {
                    if(!accountExists(name.text.toString())) {
                        bottomSheetDialog.cancel()
                        saveAccount(name.text.toString(), accountNumber.toInt())
                    } else {
                        errorMessage.isVisible = true
                        errorMessage.setText(activity.baseContext.getString(R.string.saved_account_validation_error_account_exists))
                    }
                } else {
                    errorMessage.isVisible = true
                    errorMessage.setText(activity.baseContext.getString(R.string.saved_account_validation_error_invalid_account))
                }
            }

            cancel.onClick {
                bottomSheetDialog.cancel()
                onCancel?.invoke()
            }

            bottomSheetDialog.setOnCancelListener {
                removeFromStack(bottomSheetDialog)
            }
            bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetDialog.show()
        }
    }
}
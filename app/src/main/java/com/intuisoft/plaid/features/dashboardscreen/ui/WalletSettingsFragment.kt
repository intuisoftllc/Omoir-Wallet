package com.intuisoft.plaid.features.dashboardscreen.ui

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentWalletSettingsBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.WalletSettingsViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.ConfigSeedData
import io.horizontalsystems.bitcoincore.core.Bip
import io.horizontalsystems.bitcoinkit.BitcoinKit
import org.koin.androidx.viewmodel.ext.android.viewModel


class WalletSettingsFragment : PinProtectedFragment<FragmentWalletSettingsBinding>() {
    private val viewModel: WalletSettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWalletSettingsBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel, listOf())
        return binding.root

    }


    override fun onConfiguration(configuration: FragmentConfiguration?) {

        viewModel.updateWalletSettings()
        viewModel.walletName.observe(viewLifecycleOwner, Observer {
            binding.walletNameSetting.setSubTitleText(it)
        })

        viewModel.walletBip.observe(viewLifecycleOwner, Observer {
            when(it) {
                Bip.BIP84 -> {
                    binding.bipSetting.setSubTitleText(Constants.Strings.BIP_TYPE_84)
                }
                Bip.BIP49 -> {
                    binding.bipSetting.setSubTitleText(Constants.Strings.BIP_TYPE_49)
                }
                Bip.BIP44 -> {
                    binding.bipSetting.setSubTitleText(Constants.Strings.BIP_TYPE_44)
                }
            }
        })

        viewModel.walletNetwork.observe(viewLifecycleOwner, Observer {
            if(it == BitcoinKit.NetworkType.TestNet) {
                binding.networkSetting.setSubTitleText(Constants.Strings.TEST_NET_WALLET)
            } else {
                binding.networkSetting.setSubTitleText(Constants.Strings.MAIN_NET_WALLET)
            }
        })

        binding.seedPhraseSetting.onClick {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    actionBarTitle = R.string.seed_phrase_basic_fragment_label,
                    showActionBar = true,
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

        binding.exportSetting.onClick {
            navigate(
                R.id.exportWalletFragment,
                viewModel.getWalletId()
            )
        }

        binding.walletNameSetting.onClick {
            showWalletNameDialog()
        }

        binding.deleteWalletSetting.onClick {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.delete_wallet_title)
                .setMessage(R.string.wipe_wallet_data_message)
                .setPositiveButton(R.string.erase_data) { dialog, id ->

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


    fun wipeData() {
        val progressDialog = ProgressDialog.show(requireContext(), getString(R.string.wiping_wallet_title), getString(R.string.wiping_wallet_message))
        progressDialog.setCancelable(false)

        viewModel.deleteWallet {
            progressDialog.cancel()
            navigate(R.id.splashFragment)
        }
    }

    private fun showWalletNameDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.wallet_settings_update_name)
        val walletName = bottomSheetDialog.findViewById<EditText>(R.id.walletNameOption)
        walletName?.setText(viewModel.getWalletName())

        walletName?.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // if the event is a key down event on the enter button
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    requireActivity().hideSoftKeyboard()
                    walletName.clearFocus()
                    walletName.isCursorVisible = false

                    return true
                }
                return false
            }
        })

        bottomSheetDialog.setOnCancelListener {
            viewModel.updateWalletName(walletName?.text.toString())
        }

        bottomSheetDialog.show()
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showActionBar(): Boolean {
        return true
    }

    override fun actionBarTitle(): Int {
        return R.string.wallet_settings_fragment_label
    }

    override fun navigationId(): Int {
        return R.id.settingsFragment
    }
}
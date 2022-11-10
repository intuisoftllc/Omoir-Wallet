package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentCreateImportWalletBinding
import com.intuisoft.plaid.features.createwallet.ZoomOutPageTransformer
import com.intuisoft.plaid.features.createwallet.adapters.WalletBenefitsAdapter
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.common.util.Constants
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic
import org.koin.androidx.viewmodel.ext.android.viewModel


class CreateWalletFragment : PinProtectedFragment<FragmentCreateImportWalletBinding>() {
    protected val viewModel: CreateWalletViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCreateImportWalletBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_WALLET_DATA
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.advancedOptions.setOnClickListener {
            showAdvancedOptionsDialog()
        }

        val adapter = WalletBenefitsAdapter(
            requireActivity()
        )

        binding.importWallet.onClick {
            var bundle = bundleOf(
                com.intuisoft.plaid.common.util.Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    configurationType = FragmentConfigurationType.CONFIGURATION_WALLET_DATA,
                    configData = viewModel.getConfiguration()
                )
            )

            navigate(
                R.id.importWalletFragment,
                bundle,
                com.intuisoft.plaid.common.util.Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        binding.createNewWallet.onClick {
            var bundle = bundleOf(
                com.intuisoft.plaid.common.util.Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    configurationType = FragmentConfigurationType.CONFIGURATION_WALLET_DATA,
                    configData = viewModel.getConfiguration()
                )
            )

            navigate(
                R.id.backupWalletFragment,
                bundle,
                com.intuisoft.plaid.common.util.Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        binding.useCasesViewpager.adapter = adapter
        binding.dotsIndicator.attachTo(binding.useCasesViewpager)
        binding.useCasesViewpager.setPageTransformer(ZoomOutPageTransformer())
    }

    private fun showAdvancedOptionsDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_advanced_options_create_wallet)
        val mainNet = bottomSheetDialog.findViewById<SettingsItemView>(R.id.mainNetOption)!!
        val testNet = bottomSheetDialog.findViewById<SettingsItemView>(R.id.testNetOption)!!
        val entropyStrength = bottomSheetDialog.findViewById<SettingsItemView>(R.id.entropyOption)!!
        val bip = bottomSheetDialog.findViewById<SettingsItemView>(R.id.bipOption)!!

        testNet.checkRadio(viewModel.useTestNet)
        mainNet.checkRadio(!viewModel.useTestNet)

        mainNet.onRadioClicked { view, it ->
            viewModel.setUseTestNet(!it)
            testNet.checkRadio(!it)
            mainNet.checkRadio(it)
        }


        testNet.onRadioClicked { view, it ->
            viewModel.setUseTestNet(it)
            testNet.checkRadio(it)
            mainNet.checkRadio(!it)
        }

        entropyStrength.onClick {
            bottomSheetDialog.cancel()
            showEntropyStrengthDialog()
        }

        bip.onClick {
            bottomSheetDialog.cancel()
            showBipDialog()
        }

        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetDialog.show()
    }

    private fun showEntropyStrengthDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_max_attempts)
        val title = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val numberPicker = bottomSheetDialog.findViewById<NumberPicker>(R.id.numberPicker)

        title?.setText(getString(R.string.create_wallet_advanced_options_entropy_strength))
        numberPicker?.minValue = 0
        numberPicker?.maxValue = 4
        numberPicker?.displayedValues = arrayOf(
            getString(R.string.create_wallet_advanced_options_entropy_strength_1),
            getString(R.string.create_wallet_advanced_options_entropy_strength_2),
            getString(R.string.create_wallet_advanced_options_entropy_strength_3),
            getString(R.string.create_wallet_advanced_options_entropy_strength_4),
            getString(R.string.create_wallet_advanced_options_entropy_strength_5)
        )

        when(viewModel.getEntropyStrength()) {
            Mnemonic.EntropyStrength.Default -> {
                numberPicker?.value = 0
            }
            Mnemonic.EntropyStrength.Low -> {
                numberPicker?.value = 1
            }
            Mnemonic.EntropyStrength.Medium -> {
                numberPicker?.value = 2
            }
            Mnemonic.EntropyStrength.High -> {
                numberPicker?.value = 3
            }
            Mnemonic.EntropyStrength.VeryHigh -> {
                numberPicker?.value = 4
            }
        }

        numberPicker?.wrapSelectorWheel = true
        numberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
            when(newVal) {
                0 -> {
                    viewModel.setEntropyStrength(Mnemonic.EntropyStrength.Default)
                }
                1 -> {
                    viewModel.setEntropyStrength(Mnemonic.EntropyStrength.Low)
                }
                2 -> {
                    viewModel.setEntropyStrength(Mnemonic.EntropyStrength.Medium)
                }
                3 -> {
                    viewModel.setEntropyStrength(Mnemonic.EntropyStrength.High)
                }
                4 -> {
                    viewModel.setEntropyStrength(Mnemonic.EntropyStrength.VeryHigh)
                }
            }
        }

        bottomSheetDialog.show()
    }

    private fun showBipDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_max_attempts)
        val title = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val numberPicker = bottomSheetDialog.findViewById<NumberPicker>(R.id.numberPicker)

        title?.setText(getString(R.string.create_wallet_advanced_options_bip))
        numberPicker?.minValue = 0
        numberPicker?.maxValue = 2
        numberPicker?.displayedValues = arrayOf(
            getString(R.string.create_wallet_advanced_options_bip_1),
            getString(R.string.create_wallet_advanced_options_bip_2),
            getString(R.string.create_wallet_advanced_options_bip_3)
        )

        when(viewModel.getLocalBipType()) {
            HDWallet.Purpose.BIP84 -> {
                numberPicker?.value = 0
            }
            HDWallet.Purpose.BIP49 -> {
                numberPicker?.value = 1
            }
            HDWallet.Purpose.BIP44 -> {
                numberPicker?.value = 2
            }
        }

        numberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
            when(newVal) {
                0 -> {
                    viewModel.setLocalBip(HDWallet.Purpose.BIP84)
                }
                1 -> {
                    viewModel.setLocalBip(HDWallet.Purpose.BIP49)
                }
                2 -> {
                    viewModel.setLocalBip(HDWallet.Purpose.BIP44)
                }
            }
        }

        bottomSheetDialog.show()
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.createWalletFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
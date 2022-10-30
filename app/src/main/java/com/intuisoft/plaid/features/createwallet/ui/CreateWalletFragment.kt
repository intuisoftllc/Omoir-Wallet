package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayoutMediator
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.SettingsItemView
import com.intuisoft.plaid.androidwrappers.hideSoftKeyboard
import com.intuisoft.plaid.databinding.FragmentCreateImportWalletBinding
import com.intuisoft.plaid.features.createwallet.ZoomOutPageTransformer
import com.intuisoft.plaid.features.createwallet.adapters.WalletBenefitsAdapter
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.Constants
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Mnemonic
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class CreateWalletFragment : PinProtectedFragment<FragmentCreateImportWalletBinding>() {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCreateImportWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
//        viewModel.setUseTestNet(false)
//        viewModel.setEntropyStrength(Mnemonic.EntropyStrength.Default)
//        viewModel.setLocalBip(HDWallet.Purpose.BIP84)
//
//        binding.advancedOptions.onClick {
//            showAdvancedOptionsDialog()
//        }
//
//        val adapter = WalletBenefitsAdapter(
//            requireActivity()
//        )
//
//        binding.createNewWallet.onClick {
//            findNavController().navigate(
//                CreateWalletFragmentDirections.actionCreateWalletFragmentToBackupWalletFragment(),
//                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
//            )
//        }
//
//        binding.useCasesViewpager.adapter = adapter
//        TabLayoutMediator(binding.tabLayout, binding.useCasesViewpager) { tab, position ->
//        }.attach()
//
//        binding.useCasesViewpager.setPageTransformer(ZoomOutPageTransformer())
    }

    private fun showAdvancedOptionsDialog() {
//        val bottomSheetDialog = BottomSheetDialog(requireContext())
//        bottomSheetDialog.setContentView(com.intuisoft.plaid.R.layout.advanced_options_wallet_configuration)
//        val mainNet = bottomSheetDialog.findViewById<SettingsItemView>(R.id.mainNetOption)
//        val testNet = bottomSheetDialog.findViewById<SettingsItemView>(R.id.testNetOption)
//        val passphrase = bottomSheetDialog.findViewById<SettingsItemView>(R.id.passphraseOption)
//        val entropyStrength = bottomSheetDialog.findViewById<SettingsItemView>(R.id.entropyOption)
//        val bip = bottomSheetDialog.findViewById<SettingsItemView>(R.id.bipOption)
//
//        testNet?.showCheck(viewModel.useTestNet)
//        mainNet?.showCheck(!viewModel.useTestNet)
//
//        mainNet?.onClick {
//            viewModel.setUseTestNet(false)
//            testNet?.showCheck(false)
//            mainNet.showCheck(true)
//        }
//
//        testNet?.onClick {
//            viewModel.setUseTestNet(true)
//            testNet.showCheck(true)
//            mainNet?.showCheck(false)
//        }
//
//        passphrase?.onClick {
//            bottomSheetDialog.cancel()
//        }
//
//        entropyStrength?.onClick {
//            bottomSheetDialog.cancel()
//            showEntropyStrengthDialog()
//        }
//
//        bip?.onClick {
//            bottomSheetDialog.cancel()
//            showBipDialog()
//        }
//
//        bottomSheetDialog.show()
    }

    private fun showEntropyStrengthDialog() {
//        val bottomSheetDialog = BottomSheetDialog(requireContext())
//        bottomSheetDialog.setContentView(R.layout.max_pin_attempts_bottom_sheet)
//        val numberPicker = bottomSheetDialog.findViewById<NumberPicker>(R.id.numberPicker)
//
//        numberPicker?.minValue = 0
//        numberPicker?.maxValue = 4
//        numberPicker?.displayedValues = arrayOf(
//            Constants.Strings.ENTROPY_STRENGTH_DEFAULT,
//            Constants.Strings.ENTROPY_STRENGTH_LOW,
//            Constants.Strings.ENTROPY_STRENGTH_MEDIUM,
//            Constants.Strings.ENTROPY_STRENGTH_HIGH,
//            Constants.Strings.ENTROPY_STRENGTH_VERY_HIGH
//        )
//
//        when(viewModel.getEntropyStrength()) {
//            Mnemonic.EntropyStrength.Default -> {
//                numberPicker?.value = 0
//            }
//            Mnemonic.EntropyStrength.Low -> {
//                numberPicker?.value = 1
//            }
//            Mnemonic.EntropyStrength.Medium -> {
//                numberPicker?.value = 2
//            }
//            Mnemonic.EntropyStrength.High -> {
//                numberPicker?.value = 3
//            }
//            Mnemonic.EntropyStrength.VeryHigh -> {
//                numberPicker?.value = 4
//            }
//        }
//
//        numberPicker?.wrapSelectorWheel = true
//        numberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
//            when(newVal) {
//                0 -> {
//                    viewModel.setEntropyStrength(Mnemonic.EntropyStrength.Default)
//                }
//                1 -> {
//                    viewModel.setEntropyStrength(Mnemonic.EntropyStrength.Low)
//                }
//                2 -> {
//                    viewModel.setEntropyStrength(Mnemonic.EntropyStrength.Medium)
//                }
//                3 -> {
//                    viewModel.setEntropyStrength(Mnemonic.EntropyStrength.High)
//                }
//                4 -> {
//                    viewModel.setEntropyStrength(Mnemonic.EntropyStrength.VeryHigh)
//                }
//            }
//        }
//
//        bottomSheetDialog.show()
    }

    private fun showBipDialog() {
//        val bottomSheetDialog = BottomSheetDialog(requireContext())
//        bottomSheetDialog.setContentView(R.layout.max_pin_attempts_bottom_sheet)
//        val numberPicker = bottomSheetDialog.findViewById<NumberPicker>(R.id.numberPicker)
//
//        numberPicker?.minValue = 0
//        numberPicker?.maxValue = 2
//        numberPicker?.displayedValues = arrayOf(
//            Constants.Strings.BIP_TYPE_84,
//            Constants.Strings.BIP_TYPE_49,
//            Constants.Strings.BIP_TYPE_44
//        )
//
//        when(viewModel.getLocalBipType()) {
//            HDWallet.Purpose.BIP84 -> {
//                numberPicker?.value = 0
//            }
//            HDWallet.Purpose.BIP49 -> {
//                numberPicker?.value = 1
//            }
//            HDWallet.Purpose.BIP44 -> {
//                numberPicker?.value = 2
//            }
//        }
//
//        numberPicker?.setOnValueChangedListener { picker, oldVal, newVal ->
//            when(newVal) {
//                0 -> {
//                    viewModel.setLocalBip(HDWallet.Purpose.BIP84)
//                }
//                1 -> {
//                    viewModel.setLocalBip(HDWallet.Purpose.BIP49)
//                }
//                2 -> {
//                    viewModel.setLocalBip(HDWallet.Purpose.BIP44)
//                }
//            }
//        }
//
//        bottomSheetDialog.show()
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
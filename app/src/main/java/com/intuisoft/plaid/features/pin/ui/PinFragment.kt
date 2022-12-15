package com.intuisoft.plaid.features.pin.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.intuisoft.plaid.R
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.PasscodeView.PasscodeViewType.Companion.TYPE_CHECK_PASSCODE
import com.intuisoft.plaid.androidwrappers.PasscodeView.PasscodeViewType.Companion.TYPE_SET_PASSCODE
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.FragmentPinBinding
import com.intuisoft.plaid.features.pin.viewmodel.PinViewModel
import com.intuisoft.plaid.common.util.Constants
import org.koin.android.ext.android.inject

class PinFragment: ConfigurableFragment<FragmentPinBinding>(
    secureScreen = true,
    requiresWallet = false
) {
    private val localStoreRepository: LocalStoreRepository by inject()

    var setupPin = false
    var homePassthrough = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPinBinding.inflate(inflater, container, false)
        setupPin = arguments?.get(Constants.Navigation.PIN_SETUP) as? Boolean ?: false
        homePassthrough = arguments?.get(Constants.Navigation.HOME_PASS_THROUGH) as? Boolean ?: false
        setupConfiguration(pinViewModel, listOf())
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.passcodeView.setPasscodeType(TYPE_CHECK_PASSCODE)

        if(setupPin) {
            binding.passcodeView.setFirstInputTip(getString(R.string.enter_pin_to_reset_message))
            binding.passcodeView.resetView()
            binding.passcodeView.disableFingerprint()
        } else {
            binding.passcodeView.setFirstInputTip(getString(R.string.create_pin_tip_message))
            ignoreOnBackPressed()
        }

        binding.passcodeView.setListener(object: PasscodeView.PasscodeViewListener {
            override fun onFail(wrongNumber: String?) {
                // do nothing
            }

            override fun onSuccess(number: String?) {
                pinViewModel.updatePinCheckedTime()

                if(setupPin) {
                    setupPin = false
                    binding.passcodeView.setPasscodeType(TYPE_SET_PASSCODE)
                    binding.passcodeView.setFirstInputTip(getString(R.string.create_pin_tip_message))
                    binding.passcodeView.setSecondInputTip(getString(R.string.re_enter_pin_tip_message))
                    binding.passcodeView.resetView()
                    binding.passcodeView.disablePinAttemptTracking()
                } else {
                    pinViewModel.startWalletManager()

                    if(homePassthrough) {
                        if(localStoreRepository.isProEnabled()) {
                            navigate(R.id.proHomescreenFragment, Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION)
                        } else {
                            navigate(R.id.homescreenFragment, Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION)
                        }
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }

            override fun onMaxAttempts() {
                val progressDialog = ProgressDialog.show(requireContext(), getString(R.string.wiping_data_title), getString(R.string.wiping_data_message))
                progressDialog.setCancelable(false)

                this@PinFragment.pinViewModel.onMaxAttempts {
                    progressDialog.cancel()

                    navigate(
                        R.id.splashFragment,
                        navOptions {
                            popUpTo(R.id.pinFragment) {
                                inclusive = true
                            }
                        }
                    )
                }
            }

            override fun onScanFingerprint(listener: FingerprintScanResponse) {
                validateFingerprint(
                    onSuccess = {
                        listener.onScanSuccess()
                    },

                    onError = {
                        listener.onScanFail()
                    },

                    subTitle = Constants.Strings.USE_BIOMETRIC_REASON_2,
                    negativeText = Constants.Strings.USE_PIN
                )
            }

        })
    }

    override fun actionBarVariant(): Int {
        return TopBarView.NO_BAR
    }

    override fun onNavigateTo(destination: Int) {
        // ignore
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun actionBarSubtitle(): Int {
        return 0
    }

    override fun actionBarActionLeft(): Int {
        return 0
    }

    override fun actionBarActionRight(): Int {
        return 0
    }

    override fun onActionLeft() {
        // ignore
    }

    override fun onActionRight() {
        // ignore
    }

    override fun onSubtitleClicked() {
        // ignore
    }

    override fun navigationId(): Int {
        return R.id.pinFragment
    }

    override fun onResume() {
        super.onResume()
        clearStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.intuisoft.plaid.features.pin.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentPinBinding
import com.intuisoft.plaid.features.pin.viewmodel.PinViewModel
import com.intuisoft.plaid.common.util.Constants
import org.koin.android.ext.android.inject

class PinFragment: BindingFragment<FragmentPinBinding>() {
    private val pinViewModel: PinViewModel by inject()

    var setupPin = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPinBinding.inflate(inflater, container, false)
        setupPin = arguments?.get("setupPin") as? Boolean ?: false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(setupPin) {
            binding.passcodeView.setFirstInputTip(getString(R.string.enter_pin_to_reset_message))
            binding.passcodeView.resetView()
            binding.passcodeView.disableFingerprint()
        } else {
            ignoreOnBackPressed()
        }

        binding.passcodeView.setLocalPasscode(pinViewModel.pin)
        binding.passcodeView.setListener(object: PasscodeView.PasscodeViewListener {
            override fun onFail(wrongNumber: String?) {
                // do nothing
            }

            override fun onSuccess(number: String?) {
                pinViewModel.updatePinCheckedTime()

                if(setupPin) {
                    setupPin = false
                    binding.passcodeView.setPasscodeType(PasscodeView.PasscodeViewType.TYPE_SET_PASSCODE)
                    binding.passcodeView.setFirstInputTip(getString(R.string.create_pin_tip_message))
                    binding.passcodeView.setSecondInputTip(getString(R.string.re_enter_pin_tip_message))
                    binding.passcodeView.disablePinAttemptTracking()
                    binding.passcodeView.resetView()
                } else {
                    if(binding.passcodeView.passcodeType == PasscodeView.PasscodeViewType.TYPE_SET_PASSCODE) {
                        pinViewModel.updatePin(number!!)
                    }

                    findNavController().popBackStack()
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

                    subTitle = com.intuisoft.plaid.common.util.Constants.Strings.USE_BIOMETRIC_REASON_2,
                    negativeText = com.intuisoft.plaid.common.util.Constants.Strings.USE_PIN
                )
            }

        })
    }

    override fun actionBarVariant(): Int {
        return TopBarView.NO_BAR
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
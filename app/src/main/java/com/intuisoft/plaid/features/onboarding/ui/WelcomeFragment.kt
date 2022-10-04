package com.intuisoft.plaid.features.onboarding.ui

import android.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.databinding.FragmentWelcomeBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.Constants.Limit.MAX_ALIAS_LENGTH
import com.intuisoft.plaid.util.entensions.hideSoftKeyboard
import com.intuisoft.plaid.util.entensions.ignoreOnBackPressed
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class WelcomeFragment : OnboardingFragment<FragmentWelcomeBinding>() {
    private val viewModel: OnboardingViewModel by sharedViewModel()
    override val onboardingStep = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.name.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // if the event is a key down event on the enter button
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    requireActivity().hideSoftKeyboard()
                    binding.name.clearFocus()
                    binding.name.isCursorVisible = false

                    return true
                }
                return false
            }
        })

        binding.name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.enableNextButton(s.length > 0 && s.length <= MAX_ALIAS_LENGTH)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })

        ignoreOnBackPressed()
    }

    override fun showActionBar(): Boolean {
        return false
    }

    override fun actionBarTitle(): Int {
        return 0
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onNextStep() {
        viewModel.updateAlias(binding.name.text.toString())

        findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToOnboardingPinSetupFragment(),
            Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION)
    }

    override fun onPrevStep() {
        // do nothing
    }
}
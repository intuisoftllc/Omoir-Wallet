package com.intuisoft.plaid.features.onboarding.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingFragment
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.androidwrappers.hideSoftKeyboard
import com.intuisoft.plaid.androidwrappers.ignoreOnBackPressed
import com.intuisoft.plaid.databinding.FragmentOnboardingWelcomeBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Limit.MAX_ALIAS_LENGTH
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class WelcomeFragment : BindingFragment<FragmentOnboardingWelcomeBinding>() {
    private val viewModel: OnboardingViewModel by sharedViewModel()
    private val walletManager: AbstractWalletManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOnboardingWelcomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.next.enableButton(false)
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
                binding.textLeft.text = "${s?.length}/25"
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })

        binding.next.onClick {
            onNextStep()
        }

        viewModel.advanceAllowed.observe(viewLifecycleOwner, Observer {
            binding.next.enableButton(it)
        })

        ignoreOnBackPressed()
    }
    override fun actionBarTitle(): Int {
        return 0
    }

    override fun actionBarVariant(): Int {
        return TopBarView.NO_BAR
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
        return R.id.welcomeFragment
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun onNextStep() {
        viewModel.updateAlias(binding.name.text.toString())

        findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToOnboardingPinSetupFragment(),
            com.intuisoft.plaid.common.util.Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION)
    }
}
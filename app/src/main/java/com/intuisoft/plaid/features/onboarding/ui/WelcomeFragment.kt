package com.intuisoft.plaid.features.onboarding.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingFragment
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.androidwrappers.hideSoftKeyboard
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventOnboardingStart
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Limit.MAX_ALIAS_LENGTH
import com.intuisoft.plaid.databinding.FragmentOnboardingWelcomeBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class WelcomeFragment : BindingFragment<FragmentOnboardingWelcomeBinding>() {
    private val viewModel: OnboardingViewModel by sharedViewModel()
    private val walletManager: AbstractWalletManager by inject()
    protected val eventTracker: EventTracker by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOnboardingWelcomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setSavedAlias()
        eventTracker.log(EventOnboardingStart())
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

        viewModel.userAlias.observe(viewLifecycleOwner, Observer {
            binding.name.setText(it)
        })

        binding.name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                viewModel.setNameValid(s.length in 1..MAX_ALIAS_LENGTH)
                viewModel.enableButton()
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

        setupTermsClick()
        binding.termsAgreement.setOnCheckedChangeListener { view, isChecked ->
            viewModel.updateTermsAccepted(isChecked)
            viewModel.enableButton()
        }
    }

    private fun setupTermsClick() {
        val agreeTerms = getString(R.string.welcome_agree_to_terms)
        val span = Spannable.Factory.getInstance().newSpannable(agreeTerms)
        span.setSpan(object : ClickableSpan() {
            override fun onClick(v: View) {
                Toast.makeText(requireContext(), "tos clicked", Toast.LENGTH_SHORT).show()
                onTermsOfServiceClicked()
            }
        }, 15, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(object : ClickableSpan() {
            override fun onClick(v: View) {
                Toast.makeText(requireContext(), "privacyPolicy clicked", Toast.LENGTH_SHORT).show()
                onPrivacyPolicyClicked()
            }
        }, 36, agreeTerms.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.termsText.text = span
        binding.termsText.highlightColor = Color.TRANSPARENT;
        binding.termsText.movementMethod = LinkMovementMethod.getInstance()
    }

    fun onTermsOfServiceClicked() {
        // todo: impl
    }

    fun onPrivacyPolicyClicked() {
        // todo: impl
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun onNavigateTo(destination: Int) {
        // ignore
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
            Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION)
    }
}
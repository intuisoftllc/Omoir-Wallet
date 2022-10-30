package com.intuisoft.plaid.features.onboarding.ui

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
import androidx.navigation.navOptions
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingFragment
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.androidwrappers.ignoreOnBackPressed
import com.intuisoft.plaid.databinding.FragmentOnboardingAllSetBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.features.pin.ui.PinFragmentDirections
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.Constants.Limit.MAX_ALIAS_LENGTH
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class AllSetFragment : BindingFragment<FragmentOnboardingAllSetBinding>() {
    private val viewModel: OnboardingViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOnboardingAllSetBinding.inflate(inflater, container, false)
        return binding.root

    }

    // todo: clear backstack when navigating here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.successIcon.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800)
        ignoreOnBackPressed()

        binding.gotoHomescreen.onClick {
            findNavController().navigate(AllSetFragmentDirections.actionAllSetFragmentToHomeScreenFragment(),
                Constants.Navigation.ANIMATED_FADE_IN_NAV_OPTION
            )
        }

        binding.createWallet.onClick {
            findNavController().navigate(AllSetFragmentDirections.actionAllSetFragmentToCreateWalletFragment(),
                Constants.Navigation.ANIMATED_FADE_IN_NAV_OPTION
            )
        }
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

    override fun navigationId(): Int {
        return R.id.allSetFragment
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
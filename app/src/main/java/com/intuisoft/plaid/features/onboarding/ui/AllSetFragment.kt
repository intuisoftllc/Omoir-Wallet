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
import com.intuisoft.plaid.databinding.FragmentAllSetBinding
import com.intuisoft.plaid.databinding.FragmentWelcomeBinding
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.features.pin.ui.PinFragmentDirections
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.Constants.Limit.MAX_ALIAS_LENGTH
import com.intuisoft.plaid.util.entensions.hideSoftKeyboard
import com.intuisoft.plaid.util.entensions.ignoreOnBackPressed
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class AllSetFragment : BindingFragment<FragmentAllSetBinding>() {
    private val viewModel: OnboardingViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAllSetBinding.inflate(inflater, container, false)
        return binding.root

    }

    // todo: clear backstack when navigating here
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.check.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(800)
        ignoreOnBackPressed()

        binding.home.onClick {
            findNavController().navigate(AllSetFragmentDirections.actionAllSetFragmentToHomeScreenFragment(),
                Constants.Navigation.ANIMATED_FADE_IN_NAV_OPTION
            )
        }

        binding.createWallet.onClick {
            findNavController().navigate(AllSetFragmentDirections.actionAllSetFragmentToCreateWalletFragment(), navOptions {
                popUpTo(R.id.allSetFragment) {
                    inclusive = true
                }
            })
        }
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
}
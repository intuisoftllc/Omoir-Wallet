package com.intuisoft.plaid.features.homescreen.ui

import android.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.androidwrappers.BindingFragment
import com.intuisoft.plaid.databinding.FragmentHomescreenBinding
import com.intuisoft.plaid.databinding.FragmentWelcomeBinding
import com.intuisoft.plaid.features.homescreen.viewmodel.HomeScreenViewModel
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.features.pin.viewmodel.PinViewModel
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.Constants.Limit.MAX_ALIAS_LENGTH
import com.intuisoft.plaid.util.entensions.hideSoftKeyboard
import com.intuisoft.plaid.util.entensions.ignoreOnBackPressed
import com.intuisoft.plaid.util.entensions.onBackPressedCallback
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class HomescreenFragment : PinProtectedFragment<FragmentHomescreenBinding>() {
    protected val viewModel: HomeScreenViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomescreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onBackPressedCallback {
            requireActivity().finish()
        }

        viewModel.updateGreeting()
        viewModel.homeScreenGreeting.observe(viewLifecycleOwner, Observer {
            binding.greetingMessage1.text = it.first
            binding.greetingMessage2.text = it.second
        })

        binding.settings.setOnClickListener {
            findNavController().navigate(
                HomescreenFragmentDirections.actionHomescreenFragmentToSettingsFragment(),
                Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION
            )
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
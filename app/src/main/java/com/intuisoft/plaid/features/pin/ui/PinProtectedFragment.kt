package com.intuisoft.plaid.features.pin.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.intuisoft.plaid.HomescreenFlowGraphDirections
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingFragment
import com.intuisoft.plaid.androidwrappers.RoundedButtonView
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.features.pin.viewmodel.PinViewModel
import com.intuisoft.plaid.util.Constants
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class PinProtectedFragment<T : ViewBinding> : BindingFragment<T>() {
    protected val pinViewModel: PinViewModel by sharedViewModel()

    override fun onResume() {
        super.onResume()

        pinViewModel.checkPinStatus {
            findNavController().navigate(ActionOnlyNavDirections(R.id.action_global_pinFragment),
                Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION)
        }
    }

}

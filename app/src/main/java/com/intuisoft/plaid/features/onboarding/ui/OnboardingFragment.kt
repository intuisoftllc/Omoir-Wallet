package com.intuisoft.plaid.features.onboarding.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingFragment
import com.intuisoft.plaid.androidwrappers.RoundedButtonView
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.util.Constants
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class OnboardingFragment<T : ViewBinding> : BindingFragment<T>() {
    protected val onboardingViewModel: OnboardingViewModel by sharedViewModel()

    abstract val onboardingStep: Int
    private val totalSteps = Constants.Limit.MAX_ONBOARDING_STEPS

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val header = binding.root.findViewById<LinearLayout>(R.id.header)
        header.findViewById<TextView>(R.id.step_count).text = "$onboardingStep/$totalSteps"
        header.findViewById<ProgressBar>(R.id.progress).progress = ((onboardingStep.toFloat()/totalSteps.toFloat()) * 100).toInt()

        val footer = binding.root.findViewById<ConstraintLayout>(R.id.footer)

        footer.findViewById<TextView>(R.id.back_button).isVisible = onboardingStep > 1
        footer.findViewById<TextView>(R.id.back_button).setOnClickListener {
            onPrevStep()
            findNavController().navigateUp()
        }

        footer.findViewById<RoundedButtonView>(R.id.next_button).onClick {
            onNextStep()
        }

        footer.findViewById<RoundedButtonView>(R.id.next_button).enableButton(false)
        onboardingViewModel.advanceAllowed.observe(viewLifecycleOwner, Observer {
            footer.findViewById<RoundedButtonView>(R.id.next_button).enableButton(it)
        })
    }

    override fun showActionBar(): Boolean {
        return false
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    abstract fun onNextStep()
    abstract fun onPrevStep()
}

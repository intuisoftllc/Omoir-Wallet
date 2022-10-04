package com.intuisoft.plaid.features.splash.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.androidwrappers.BindingFragment
import com.intuisoft.plaid.databinding.FragmentSplashBinding
import com.intuisoft.plaid.features.splash.viewmodel.SplashViewModel
import com.intuisoft.plaid.util.Constants
import org.koin.android.ext.android.inject

class SplashFragment : BindingFragment<FragmentSplashBinding>() {
    private val viewModel: SplashViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateLogo()
        viewModel.nextScreen()

        viewModel.resetPinCheckedTime()
        viewModel.nextDestination.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(it, Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION)
        })
    }

    override fun showActionBar(): Boolean {
        return false
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    fun animateLogo() {
        val animator = ObjectAnimator.ofFloat(binding.logo, View.ALPHA, 0f, 1f)
        animator.setDuration(2000)

        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
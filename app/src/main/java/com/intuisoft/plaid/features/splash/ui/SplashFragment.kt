package com.intuisoft.plaid.features.splash.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentSplashBinding
import com.intuisoft.plaid.features.splash.viewmodel.SplashViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.ConfigQrDisplayData
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
        ignoreOnBackPressed()
        viewModel.nextScreen()

        viewModel.nextDestination.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(it, Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION)
        })

        viewModel.goHome.observe(viewLifecycleOwner, Observer {
            navigate(
                R.id.pinFragment,
                bundleOf(Constants.Navigation.HOME_PASS_THROUGH to true)
            )
        })
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

    override fun onNavigateTo(destination: Int) {
        // ignore
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

    override fun actionBarVariant(): Int {
        return TopBarView.NO_BAR
    }

    override fun navigationId(): Int {
        return R.id.splashFragment
    }

    fun animateLogo() {
        val animator = ObjectAnimator.ofFloat(binding.logo, View.ALPHA, 0f, 1f)
        animator.setDuration(SplashViewModel.SPLASH_DURATION.toLong())

        animator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
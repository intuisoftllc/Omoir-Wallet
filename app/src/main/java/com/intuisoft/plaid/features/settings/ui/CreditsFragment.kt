package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.ConfigurableFragment
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.androidwrappers.openLink
import com.intuisoft.plaid.databinding.FragmentCreditsBinding
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class CreditsFragment : ConfigurableFragment<FragmentCreditsBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    private val viewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCreditsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.acknowledgement1.onClick {
            requireContext().openLink(getString(R.string.credits_ui_designer_link))
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun actionBarVariant(): Int {
        return TopBarView.CENTER_ALIGN
    }

    override fun actionBarTitle(): Int {
        return R.string.credits_fragment_label
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun navigationId(): Int {
        return R.id.creditsFragment
    }
}
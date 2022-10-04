package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import com.intuisoft.plaid.MainActivity
import com.intuisoft.plaid.R
import com.intuisoft.plaid.databinding.FragmentAboutUsBinding
import com.intuisoft.plaid.databinding.FragmentAppearanceBinding
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.model.AppTheme
import com.intuisoft.plaid.model.BitcoinDisplayUnit
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class AboutUsFragment : PinProtectedFragment<FragmentAboutUsBinding>() {
    private val viewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAboutUsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.security.setOnClickListener {
            // todo: impl -> navigate to fragment in app explaining our security model
        }

        binding.privacyPolicy.setOnClickListener {
            // todo: impl -> navigate to web address on plaidcryptowallet.com
        }

        binding.termsOfService.setOnClickListener {
            // todo: impl -> navigate to web address on plaidcryptowallet.com
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun showActionBar(): Boolean {
        return true
    }

    override fun actionBarTitle(): Int {
        return R.string.about_us_fragment_label
    }
}
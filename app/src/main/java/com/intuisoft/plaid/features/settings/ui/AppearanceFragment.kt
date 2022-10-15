package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.databinding.FragmentAppearanceBinding
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.model.AppTheme
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class AppearanceFragment : PinProtectedFragment<FragmentAppearanceBinding>() {
    private val viewModel: SettingsViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAppearanceBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        viewModel.updateAppThemeSetting()
        viewModel.appThemeSetting.observe(viewLifecycleOwner, Observer {
            (requireActivity() as MainActivity).isActionBarShowing = showActionBar()
            (requireActivity() as MainActivity).actionBarTitle = getString(actionBarTitle())

            binding.lightMode.showCheck(it == AppTheme.LIGHT)
            binding.darkMode.showCheck(it == AppTheme.DARK)
            binding.autoTheme.showCheck(it == AppTheme.AUTO)
        })

        binding.lightMode.setOnClickListener {
            viewModel.saveAppTheme(AppTheme.LIGHT)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding.darkMode.setOnClickListener {
            viewModel.saveAppTheme(AppTheme.DARK)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        binding.autoTheme.setOnClickListener {
            viewModel.saveAppTheme(AppTheme.AUTO)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
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
        return R.string.appearance_fragment_label
    }

    override fun navigationId(): Int {
        return R.id.appearanceFragment
    }
}
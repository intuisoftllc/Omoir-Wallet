package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.ConfigurableFragment
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.TopBarView
import com.intuisoft.plaid.databinding.FragmentAppearanceBinding
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.common.model.AppTheme
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class AppearanceFragment : ConfigurableFragment<FragmentAppearanceBinding>(pinProtection = true) {
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
            binding.appearanceLight.checkRadio(it == AppTheme.LIGHT)
            binding.appearanceDark.checkRadio(it == AppTheme.DARK)
            binding.appearanceAuto.checkRadio(it == AppTheme.AUTO)
        })

        binding.appearanceLight.onRadioClicked { settingsItemView, checked ->
            if(checked) {
                viewModel.saveAppTheme(AppTheme.LIGHT)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.appearanceDark.onRadioClicked { settingsItemView, checked ->
            if(checked) {
                viewModel.saveAppTheme(AppTheme.DARK)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        binding.appearanceAuto.onRadioClicked { settingsItemView, checked ->
            if(checked) {
                viewModel.saveAppTheme(AppTheme.AUTO)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
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
        return R.string.appearance_fragment_label
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun navigationId(): Int {
        return R.id.appearanceFragment
    }
}
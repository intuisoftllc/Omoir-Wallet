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
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.EventSettingsChangeAppearance
import com.intuisoft.plaid.common.analytics.events.EventSettingsView
import com.intuisoft.plaid.databinding.FragmentAppearanceBinding
import com.intuisoft.plaid.features.settings.viewmodel.SettingsViewModel
import com.intuisoft.plaid.common.model.AppTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class AppearanceFragment : ConfigurableFragment<FragmentAppearanceBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    private val viewModel: SettingsViewModel by sharedViewModel()
    protected val eventTracker: EventTracker by inject()

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
            if(checked && viewModel.getAppTheme() != AppTheme.LIGHT) {
                eventTracker.log(EventSettingsChangeAppearance(viewModel.getAppTheme()))
                viewModel.saveAppTheme(AppTheme.LIGHT)
            }
        }

        binding.appearanceDark.onRadioClicked { settingsItemView, checked ->
            if(checked && viewModel.getAppTheme() != AppTheme.DARK) {
                eventTracker.log(EventSettingsChangeAppearance(viewModel.getAppTheme()))
                viewModel.saveAppTheme(AppTheme.DARK)
            }
        }

        binding.appearanceAuto.onRadioClicked { settingsItemView, checked ->
            if(checked && viewModel.getAppTheme() != AppTheme.AUTO) {
                eventTracker.log(EventSettingsChangeAppearance(viewModel.getAppTheme()))
                viewModel.saveAppTheme(AppTheme.AUTO)
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
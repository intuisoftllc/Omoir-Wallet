package com.intuisoft.plaid.features.pin.ui

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.ConfigurableFragment
import com.intuisoft.plaid.androidwrappers.navigate
import com.intuisoft.plaid.features.pin.viewmodel.PinViewModel
import com.intuisoft.plaid.common.util.Constants
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class PinProtectedFragment<T : ViewBinding> : ConfigurableFragment<T>() {
    protected val pinViewModel: PinViewModel by sharedViewModel()

    override fun onResume() {
        super.onResume()

        // this prevents other threads that may be resuming in paralell to generate multiple pin screens
        if(findNavController().currentDestination?.id == navigationId()) {
            pinViewModel.checkPinStatus {
                navigate(
                    R.id.pinFragment,
                    Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION
                )
            }
        }
    }

}

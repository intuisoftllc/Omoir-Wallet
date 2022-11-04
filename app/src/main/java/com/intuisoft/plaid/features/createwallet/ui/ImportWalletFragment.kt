package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentImportWalletBinding
import com.intuisoft.plaid.databinding.FragmentNameWalletBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.AllSetData
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class ImportWalletFragment : PinProtectedFragment<FragmentImportWalletBinding>() {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentImportWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        viewModel.setLocalPublicKey("")
        viewModel.setLocalSeedPhrase(listOf())

        binding.publicKeyImport.onClick {
            navigate(
                R.id.publicKeyImportFragment,
                Constants.Navigation.ANIMATED_SLIDE_UP_OPTION
            )
        }

        binding.recoveryPhraseImport.onClick {
            navigate(
                R.id.recoveryPhraseImportFragment,
                Constants.Navigation.ANIMATED_SLIDE_UP_OPTION
            )
        }
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.importWalletFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
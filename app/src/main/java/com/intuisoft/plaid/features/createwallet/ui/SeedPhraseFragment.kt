package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.databinding.FragmentBackupWalletBinding
import com.intuisoft.plaid.databinding.FragmentCreateImportNonCustodialBinding
import com.intuisoft.plaid.databinding.FragmentCreateImportPrivateAndSecureBinding
import com.intuisoft.plaid.databinding.FragmentSeedPhraseBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.Constants
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SeedPhraseFragment : PinProtectedFragment<FragmentSeedPhraseBinding>() {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSeedPhraseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.seedPhrase.resetView()
        viewModel.generateNewWallet()

        viewModel.seedPhrase.observe(viewLifecycleOwner, Observer {
            it.forEach {  word ->
                binding.seedPhrase.nextWord(word)
            }
        })

        binding.confirm.onClick {
            findNavController().navigate(
                SeedPhraseFragmentDirections.actionSeedPhraseFragmentToNameWalletFragment(),
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }
    }

    override fun showActionBar(): Boolean {
        return false
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.seedPhraseFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
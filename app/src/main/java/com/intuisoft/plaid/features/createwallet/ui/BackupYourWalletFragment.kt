package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.databinding.FragmentBackupBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.Constants
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class BackupYourWalletFragment : PinProtectedFragment<FragmentBackupBinding>() {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
//        binding.confirm.enableButton(false)
//        binding.ackowledgement1.setOnCheckedChangeListener { compoundButton, isChecked ->
//            binding.confirm.enableButton(allAcknowledgementsChecked())
//        }
//
//        binding.ackowledgement2.setOnCheckedChangeListener { compoundButton, isChecked ->
//            binding.confirm.enableButton(allAcknowledgementsChecked())
//        }
//
//        binding.ackowledgement3.setOnCheckedChangeListener { compoundButton, isChecked ->
//            binding.confirm.enableButton(allAcknowledgementsChecked())
//        }
//
//        binding.confirm.onClick {
//            findNavController().navigate(
//                BackupYourWalletFragmentDirections.actionBackupWalletFragmentToSeedPhraseFragment(),
//                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
//            )
//        }
    }

    fun allAcknowledgementsChecked() : Boolean {
        return true
//        return binding.ackowledgement1.isChecked && binding.ackowledgement2.isChecked
//                && binding.ackowledgement3.isChecked
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.backupWalletFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
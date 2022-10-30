package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.hideSoftKeyboard
import com.intuisoft.plaid.androidwrappers.navigate
import com.intuisoft.plaid.androidwrappers.styledSnackBar
import com.intuisoft.plaid.databinding.FragmentNameWalletBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.Constants
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class NameYourWalletFragment : PinProtectedFragment<FragmentNameWalletBinding>() {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentNameWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
//        binding.confirm.enableButton(false)
//        binding.name.setOnKeyListener(object : View.OnKeyListener {
//            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
//                // if the event is a key down event on the enter button
//                if (event.action == KeyEvent.ACTION_DOWN &&
//                    keyCode == KeyEvent.KEYCODE_ENTER
//                ) {
//                    requireActivity().hideSoftKeyboard()
//                    binding.name.clearFocus()
//                    binding.name.isCursorVisible = false
//
//                    binding.confirm.requestFocus()
//                    return true
//                }
//                return false
//            }
//        })
//
//        binding.name.addTextChangedListener(object : TextWatcher {
//            override fun afterTextChanged(s: Editable) {
//                binding.confirm.enableButton(s.length > 0 && s.length <= Constants.Limit.MAX_ALIAS_LENGTH)
//            }
//
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//            }
//        })
//
//        binding.confirm.onClick {
//            viewModel.commitWalletToDisk(binding.name.text.toString())
//        }
//
//        viewModel.walletCreationError.observe(viewLifecycleOwner, Observer {
//            styledSnackBar(requireView(), "Oops, failed to create wallet, please try again.")
//        })
//
//        viewModel.walletCreated.observe(viewLifecycleOwner, Observer {
//            navigate(
//                R.id.walletCreatedFragment,
//                it,
//                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
//            )
//        })
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.nameWalletFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
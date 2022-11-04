package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentImportWalletBinding
import com.intuisoft.plaid.databinding.FragmentNameWalletBinding
import com.intuisoft.plaid.databinding.FragmentPublicKeyImportBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.listeners.BarcodeResultListener
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.AllSetData
import kotlinx.android.synthetic.main.fragment_public_key_import.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class PublicKeyImportFragment : PinProtectedFragment<FragmentPublicKeyImportBinding>(), BarcodeResultListener {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPublicKeyImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        binding.scan.onClick {
            scanBarcode()
        }

        binding.importKey.onClick {
            viewModel.setLocalPublicKey(binding.publicKey.text.toString())
            viewModel.importPublicKey()
        }

        viewModel.onConfirm.observe(viewLifecycleOwner, Observer {
            navigate(
                R.id.nameWalletFragment,
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        })

        viewModel.onInputRejected.observe(viewLifecycleOwner, Observer {
            onInvalidBitcoinAddressAnimation()
        })

        viewModel.onDisplayExplanation.observe(viewLifecycleOwner, Observer {
            styledSnackBar(requireView(), it, true)
        })
    }

    fun onInvalidBitcoinAddressAnimation() {
        val shake: Animation = AnimationUtils.loadAnimation(requireActivity(), R.anim.shake)
        binding.publicKey.startAnimation(shake)
    }

    override fun onAddressReceived(address: String) {
        binding.publicKey.setText(address)
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.publicKeyImportFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
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
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.FragmentNameWalletBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.fragmentconfig.AllSetData
import com.intuisoft.plaid.util.fragmentconfig.WalletConfigurationData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class NameYourWalletFragment : ConfigurableFragment<FragmentNameWalletBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    protected val viewModel: CreateWalletViewModel by viewModel()
    protected val walletManager: AbstractWalletManager by inject()
    protected val localStoreRepository: LocalStoreRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentNameWalletBinding.inflate(inflater, container, false)
        setupConfiguration(viewModel,
            listOf(
                FragmentConfigurationType.CONFIGURATION_WALLET_DATA
            )
        )
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        viewModel.setConfiguration(configuration!!.configData as WalletConfigurationData)

        binding.confirm.enableButton(false)
        binding.name.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                // if the event is a key down event on the enter button
                if (event.action == KeyEvent.ACTION_DOWN &&
                    keyCode == KeyEvent.KEYCODE_ENTER
                ) {
                    requireActivity().hideSoftKeyboard()
                    binding.name.clearFocus()
                    binding.name.isCursorVisible = false

                    binding.confirm.requestFocus()
                    return true
                }
                return false
            }
        })

        binding.name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                binding.confirm.enableButton(s.length > 0 && s.length <= Constants.Limit.MAX_ALIAS_LENGTH)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }
        })

        binding.confirm.onClick {
            binding.loading.isVisible = true
            binding.confirm.enableButton(false)
            viewModel.commitWalletToDisk(binding.name.text.toString())
        }

        viewModel.walletCreationError.observe(viewLifecycleOwner, Observer {
            binding.loading.isVisible = false
            binding.confirm.enableButton(true)
            styledSnackBar(requireView(), getString(R.string.create_wallet_failure_error), true)
        })

        viewModel.walletCreated.observe(viewLifecycleOwner, Observer {
            walletManager.openWallet(walletManager.findLocalWallet(it)!!)

            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration( // todo: check for pro here for home screen destination and on all other places as well
                    actionBarTitle = 0,
                    actionBarSubtitle = 0,
                    actionBarVariant = 0,
                    actionLeft = 0,
                    actionRight = 0,
                    configurationType = FragmentConfigurationType.CONFIGURATION_All_SET,
                    configData = AllSetData(
                        title = getString(R.string.create_wallet_success_title),
                        subtitle = getString(R.string.create_wallet_success_subtitle),
                        positiveText = getString(R.string.create_wallet_success_positive_button),
                        negativeText = getString(R.string.create_wallet_success_negative_button),
                        positiveDestination = if(localStoreRepository.isProEnabled()) R.id.walletProDashboardFragment else R.id.walletDashboardFragment,
                        negativeDestination = if(localStoreRepository.isProEnabled()) R.id.proHomescreenFragment else R.id.homescreenFragment,
                        walletUUID = it
                    ),
                )
            )

            navigate(
                R.id.allSetFragment,
                bundle
            )
        })
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
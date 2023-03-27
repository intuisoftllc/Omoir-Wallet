package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.delegates.FragmentConfiguration
import com.intuisoft.plaid.common.model.HiddenWalletModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.FragmentNameWalletBinding
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.SavedAccountsAdapter
import com.intuisoft.plaid.features.dashboardflow.shared.ui.WalletSettingsFragment
import com.intuisoft.plaid.features.settings.ui.SettingsFragment
import com.intuisoft.plaid.util.fragmentconfig.AllSetData
import com.intuisoft.plaid.util.fragmentconfig.WalletConfigurationData
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import io.horizontalsystems.bitcoincore.core.description
import io.horizontalsystems.hdwalletkit.HDExtendedKeyVersion
import io.horizontalsystems.hdwalletkit.HDWallet
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

        binding.configuration.setOnSingleClickListener {
            showWalletConfiguration()
        }

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
            viewModel.commitWalletToDisk(binding.name.text.toString().trim())
        }

        viewModel.walletCreationError.observe(viewLifecycleOwner, Observer {
            binding.loading.isVisible = false
            binding.confirm.enableButton(true)
            styledSnackBar(requireView(), getString(R.string.create_wallet_failure_error, it.message), true)
        })

        viewModel.walletCreated.observe(viewLifecycleOwner, Observer {
            var bundle = bundleOf(
                Constants.Navigation.FRAGMENT_CONFIG to FragmentConfiguration(
                    configurationType = FragmentConfigurationType.CONFIGURATION_All_SET,
                    configData = AllSetData(
                        title = getString(R.string.create_wallet_success_title),
                        subtitle = getString(R.string.create_wallet_success_subtitle),
                        positiveText = getString(R.string.create_wallet_success_positive_button),
                        negativeText = getString(R.string.create_wallet_success_negative_button),
                        positiveDestination = if(localStoreRepository.isPremiumUser()) R.id.walletProDashboardFragment else R.id.walletDashboardFragment,
                        negativeDestination = if(localStoreRepository.isPremiumUser()) R.id.proHomescreenFragment else R.id.homescreenFragment,
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

    fun showWalletConfiguration() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        var doNotRecreate = false
        var showingSeed = false
        addToStack(bottomSheetDialog) {
            doNotRecreate = true
        }
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_wallet_configuration)
        val network = bottomSheetDialog.findViewById<SettingsItemView>(R.id.network)!!
        val gapLimit = bottomSheetDialog.findViewById<SettingsItemView>(R.id.gap_limit)!!
        val bip = bottomSheetDialog.findViewById<SettingsItemView>(R.id.bip)!!
        val seedKeyTitle = bottomSheetDialog.findViewById<TextView>(R.id.seed_or_key_title)!!
        val seed = bottomSheetDialog.findViewById<TextView>(R.id.seed_or_key)!!
        val showHide = bottomSheetDialog.findViewById<ImageView>(R.id.show_hide)!!

        network.setSubTitleText(
            if(viewModel.useTestNet)
                getString(R.string.test_net)
            else getString(R.string.main_net)
        )

        gapLimit.setSubTitleText(
            "${viewModel.gapLimit}"
        )

        bip.setSubTitleText(
            when(viewModel.getLocalBipType()) {
                HDWallet.Purpose.BIP84 -> {
                    getString(R.string.create_wallet_advanced_options_bip_1)
                }

                HDWallet.Purpose.BIP49-> {
                    getString(R.string.create_wallet_advanced_options_bip_2)
                }

                HDWallet.Purpose.BIP44 -> {
                    getString(R.string.create_wallet_advanced_options_bip_3)
                }
            }
        )

        network.onClick {
            bottomSheetDialog.cancel()
            showNetworkDialog()
        }

        gapLimit.onClick {
            bottomSheetDialog.cancel()
            SettingsFragment.showGapLimitDialog(
                activity = requireActivity(),
                addToStack = ::addToStack,
                removeFromStack = ::removeFromStack,
                getGapLimit = {
                    viewModel.gapLimit
                },
                setGapLimit = {
                    viewModel.gapLimit = it
                },
                onDismiss = {
                    showWalletConfiguration()
                }
            )
        }

        bip.onClick {
            bottomSheetDialog.cancel()
            CreateWalletFragment.showBipDialog(
                activity = requireActivity(),
                addToStack = ::addToStack,
                removeFromStack = ::removeFromStack,
                getBip = {
                    viewModel.getLocalBipType()
                },
                setBip = {
                    viewModel.setLocalBip(it)
                },
                onDismiss = {
                    showWalletConfiguration()
                }
            )
        }

        if(viewModel.getLocalPublicKey().isNotEmpty()) {
            network.disableView(true)
            bip.disableView(true)

            val prefix = viewModel.getLocalPublicKey().take(4)
            HDExtendedKeyVersion.values().find { it.base58Prefix == prefix }?.let {
                viewModel.setUseTestNet(it.isTest)
                viewModel.setLocalBip(it.purpose)
            }

            seedKeyTitle.text = getString(R.string.create_wallet_configuration_item_pub_priv_title)
        } else {
            seedKeyTitle.text = getString(R.string.create_wallet_configuration_item_seed_title)
        }

        showHide.setOnSingleClickListener {
            showingSeed = !showingSeed

            if(showingSeed) {
                showHide.setImageResource(R.drawable.ic_eye_open)

                if(viewModel.getLocalPublicKey().isNotEmpty()) {
                    seed.text = viewModel.getLocalPublicKey()
                } else {
                    seed.text = viewModel.getLocalSeedPhrase().joinToString(" ")
                }
            } else {
                showHide.setImageResource(R.drawable.ic_eye_closed)
                seed.text = getString(R.string.private_symbol)
            }
        }

        bottomSheetDialog.setOnCancelListener {
            removeFromStack(bottomSheetDialog)
        }
        bottomSheetDialog.show()
    }

    fun showNetworkDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        var doNotRecreate = false
        addToStack(bottomSheetDialog) {
            doNotRecreate = true
        }
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_data_type_filter)
        val title = bottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)!!
        val mainNet = bottomSheetDialog.findViewById<SettingsItemView>(R.id.transaction_type_raw)!!
        val testNet = bottomSheetDialog.findViewById<SettingsItemView>(R.id.transaction_type_incoming)!!
        val done = bottomSheetDialog.findViewById<RoundedButtonView>(R.id.done)!!
        bottomSheetDialog.findViewById<SettingsItemView>(R.id.transaction_type_outgoing)!!.isVisible = false
        title.text = getString(R.string.choose_network)
        mainNet.setTitleText(getString(R.string.main_net))
        testNet.setTitleText(getString(R.string.test_net))

        mainNet.checkRadio(!viewModel.useTestNet)
        testNet.checkRadio(viewModel.useTestNet)

        testNet.onRadioClicked { view, clicked ->
            if(clicked) {
                viewModel.setUseTestNet(true)
                mainNet.checkRadio(false)
            }
        }

        mainNet.onRadioClicked { view, clicked ->
            if(clicked) {
                viewModel.setUseTestNet(false)
                testNet.checkRadio(false)
            }
        }

        done.onClick {
            bottomSheetDialog.cancel()
        }

        bottomSheetDialog.setOnCancelListener {
            if(!doNotRecreate) {
                removeFromStack(bottomSheetDialog)
                showWalletConfiguration()
            }
        }
        bottomSheetDialog.show()
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
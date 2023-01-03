package com.intuisoft.plaid.features.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.analytics.EventTracker
import com.intuisoft.plaid.common.analytics.events.*
import com.intuisoft.plaid.common.model.SavedAccountModel
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.FragmentAccountsBinding
import com.intuisoft.plaid.features.dashboardflow.shared.adapters.SavedAccountsAdapter
import com.intuisoft.plaid.features.dashboardflow.shared.ui.WalletSettingsFragment
import com.intuisoft.plaid.features.settings.viewmodel.AccountsViewModel
import io.horizontalsystems.hdwalletkit.HDWallet
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class AccountsFragment : ConfigurableFragment<FragmentAccountsBinding>(
    pinProtection = true,
    requiresWallet = false
) {
    private val viewModel: AccountsViewModel by viewModel()
    protected val eventTracker: EventTracker by inject()

    private val adapter = SavedAccountsAdapter(
        onItemSelected = ::onAccountSelected
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {

        viewModel.showAccounts()
        binding.savedAccounts.adapter = adapter
        viewModel.accounts.observe(viewLifecycleOwner, Observer {
            adapter.addSavedAccounts(it.toArrayList())

            binding.savedAccounts.isVisible = it.isNotEmpty()
            binding.noAccountsContainer.isVisible = it.isEmpty()
        })
    }

    private fun onAccountSelected(account: SavedAccountModel) {
        if(account.canDelete) {
            WalletSettingsFragment.showSaveAccountDialog(
                activity = requireActivity(),
                titleText = getString(R.string.update_account_update_title),
                saveButtonText = getString(R.string.update),
                cancelButtonText = getString(R.string.delete),
                initialNameText = account.accountName,
                initialAccountText = account.account.toString(),
                getDerivaitionPath = {
                    "m/[84|44|49]'/0'/$it'"
                },
                saveAccount = { name, accountNumber ->
                    eventTracker.log(EventSettingsUpdateAccount())
                    viewModel.updateAccount(account.accountName, name, accountNumber)
                    viewModel.showAccounts()
                },
                onCancel = {
                    if (!viewModel.isAccountInUse(account.accountName)) {
                        SettingsFragment.warningDialog(
                            context = requireContext(),
                            title = getString(R.string.save_account_delete_title),
                            subtitle = getString(
                                R.string.save_account_delete_subtitle,
                                account.accountName
                            ),
                            positive = getString(R.string.delete),
                            negative = getString(R.string.cancel),
                            positiveTint = 0,
                            onPositive = {
                                eventTracker.log(EventSettingsDeleteAccount())
                                viewModel.deleteAccount(account.accountName)
                                viewModel.showAccounts()
                            },
                            onNegative = {
                                // do nothin
                            },
                            addToStack = ::addToStack,
                            removeFromStack = ::removeFromStack
                        )
                    } else {
                        styledSnackBar(
                            requireView(),
                            getString(R.string.save_account_delete_failure)
                        )
                    }
                },
                addToStack = ::addToStack,
                removeFromStack = ::removeFromStack
            )
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
        return R.string.accounts_fragment_label
    }

    override fun actionBarActionLeft(): Int {
        return R.drawable.ic_arrow_left
    }

    override fun actionBarActionRight(): Int {
        return R.drawable.ic_add
    }

    override fun onActionLeft() {
        findNavController().popBackStack()
    }

    override fun onActionRight() {
        WalletSettingsFragment.showSaveAccountDialog(
            activity = requireActivity(),
            titleText = getString(R.string.saved_account_save_title),
            saveButtonText = getString(R.string.save),
            getDerivaitionPath = {
                "m/${HDWallet.Purpose.BIP84.value}'/0'/$it'"
            },
            cancelButtonText = getString(R.string.cancel),
            saveAccount = { name, accountNumber ->
                eventTracker.log(EventSettingsSaveAccount())
                viewModel.saveAccount(name, accountNumber)
            },
            onCancel = {},
            addToStack = ::addToStack,
            removeFromStack = ::removeFromStack
        )
    }

    override fun navigationId(): Int {
        return R.id.accountsFragment
    }
}
package com.intuisoft.plaid.features.dashboardscreen.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.docformative.docformative.toArrayList
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.WalletViewModel
import com.intuisoft.plaid.androidwrappers.navigate
import com.intuisoft.plaid.databinding.FragmentWalletDashboardBinding
import com.intuisoft.plaid.features.createwallet.ui.BackupYourWalletFragmentDirections
import com.intuisoft.plaid.features.homescreen.adapters.BasicTransactionAdapter
import com.intuisoft.plaid.features.homescreen.adapters.BasicWalletDataAdapter
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.model.WalletState
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.walletmanager.ManagerState
import com.intuisoft.plaid.walletmanager.WalletManager
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DashboardFragment : PinProtectedFragment<FragmentWalletDashboardBinding>() {
    protected val viewModel: WalletViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: WalletManager by inject()

    private val adapter = BasicTransactionAdapter(
        onTransactionSelected = ::onTransactionSelected,
        localStoreRepository = localStoreRepository
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWalletDashboardBinding.inflate(inflater, container, false)

        setupConfiguration(viewModel)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {
        viewModel.getTransactions()
        viewModel.displayCurrentWallet()
        walletManager.stateChanged.observe(viewLifecycleOwner, Observer {
            binding.swipeContainer.isRefreshing = it == ManagerState.SYNCHRONIZING
        })

        binding.swipeContainer.setOnRefreshListener {
            if(binding.swipeContainer.isRefreshing) {
                viewModel.syncWallet()
            }
        }

        viewModel.displayWallet.observe(viewLifecycleOwner, Observer { wallet ->
            binding.walletNameView.text = wallet.name
            binding.balance.text = wallet.getBalance(localStoreRepository, true)
            wallet.walletStateUpdated.observe(viewLifecycleOwner, Observer {
                wallet.onWalletStateChanged(binding.balance, it, true, localStoreRepository)

                if(wallet.walletState == WalletState.NONE)
                    viewModel.getTransactions()
            })

        })

        binding.transactions.adapter = adapter
        viewModel.transactions.observe(viewLifecycleOwner, Observer {
            binding.noTransactionsIcon.isVisible = it.isEmpty()
            binding.noTransactionsMessage.isVisible = it.isEmpty()
            binding.transactions.isVisible = it.isNotEmpty()


            adapter.addTransactions(it.toArrayList())
        })

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.settings.setOnClickListener {
            navigate(
                R.id.walletSettingsFragment,
                viewModel.getWalletId()
            )
        }
    }

    fun onTransactionSelected(transaction: TransactionInfo) {
        navigate(
            R.id.transactionDetailsFragment,
            viewModel.getWalletId(),
            Constants.Navigation.ANIMATED_FADE_IN_NAV_OPTION
        )
    }

    override fun showActionBar(): Boolean {
        return false
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.walletDashboardFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
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
import com.intuisoft.plaid.databinding.FragmentWalletDashboardBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.DashboardViewModel
import com.intuisoft.plaid.features.homescreen.adapters.BasicTransactionAdapter
import com.intuisoft.plaid.features.homescreen.adapters.BasicWalletDataAdapter
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.walletmanager.ManagerState
import com.intuisoft.plaid.walletmanager.WalletManager
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DashboardFragment : PinProtectedFragment<FragmentWalletDashboardBinding>() {
    protected val viewModel: DashboardViewModel by sharedViewModel()
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

        if(arguments != null) {
            viewModel.getWallet(requireArguments().getString(Constants.Navigation.WALLET_NAME_BUNDLE_ID) ?: "")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.createSubscriptions()
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
            binding.balance.text = wallet.getBalance(localStoreRepository)
            wallet.walletStateUpdated.observe(viewLifecycleOwner, Observer {
                wallet.onWalletStateChanged(binding.balance, it, localStoreRepository)
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
    }

    fun onTransactionSelected(transaction: TransactionInfo) {

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
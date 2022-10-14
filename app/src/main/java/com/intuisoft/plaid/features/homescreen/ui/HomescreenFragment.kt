package com.intuisoft.plaid.features.homescreen.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.docformative.docformative.toArrayList
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.FragmentConfiguration
import com.intuisoft.plaid.androidwrappers.navigate
import com.intuisoft.plaid.androidwrappers.onBackPressedCallback
import com.intuisoft.plaid.databinding.FragmentHomescreenBinding
import com.intuisoft.plaid.features.homescreen.adapters.BasicTransactionAdapter
import com.intuisoft.plaid.features.homescreen.adapters.BasicWalletDataAdapter
import com.intuisoft.plaid.features.homescreen.viewmodel.HomeScreenViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.walletmanager.ManagerState
import com.intuisoft.plaid.walletmanager.WalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomescreenFragment : PinProtectedFragment<FragmentHomescreenBinding>() {
    protected val viewModel: HomeScreenViewModel by viewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: WalletManager by inject()

    private val adapter = BasicWalletDataAdapter(
        onWalletSelected = ::onWalletSelected,
        localStoreRepository = localStoreRepository
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomescreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onConfiguration(configuration: FragmentConfiguration?) {

        onBackPressedCallback {
            requireActivity().finish()
        }

        viewModel.updateGreeting()
        viewModel.showWallets()
        viewModel.syncWallets()

        walletManager.stateChanged.observe(viewLifecycleOwner, Observer {
            binding.swipeContainer.isRefreshing = it == ManagerState.SYNCHRONIZING
        })

        binding.swipeContainer.setOnRefreshListener {
            if(binding.swipeContainer.isRefreshing) {
                walletManager.synchronizeAll()
            } else {
                viewModel.showWallets()
            }
        }

        binding.walletsList.adapter = adapter
        viewModel.homeScreenGreeting.observe(viewLifecycleOwner, Observer {
            binding.greetingMessage1.text = it.first
            binding.greetingMessage2.text = it.second
        })

        viewModel.wallets.observe(viewLifecycleOwner, Observer {
            adapter.addWallets(it.toArrayList())

            binding.walletsList.isVisible = it.isNotEmpty()
            binding.noWalletsImg.isVisible = it.isEmpty()
            binding.noWalletsMessage.isVisible = it.isEmpty()
        })

        binding.settings.setOnClickListener {
            navigate(R.id.settingsFragment)
        }

        binding.addWallet.setOnClickListener {
            navigate(R.id.createWalletFragment)
        }
    }

    fun onWalletSelected(wallet: LocalWalletModel) {
        navigate(R.id.walletDashboardFragment, wallet)
    }

    override fun showActionBar(): Boolean {
        return false
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.homescreenFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
package com.intuisoft.plaid.features.homescreen.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.app.ActivityOptionsCompat
import androidx.core.os.bundleOf
import androidx.core.util.Pair
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.docformative.docformative.toArrayList
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.BindingFragment
import com.intuisoft.plaid.androidwrappers.onBackPressedCallback
import com.intuisoft.plaid.androidwrappers.styledSnackBar
import com.intuisoft.plaid.databinding.FragmentHomescreenBinding
import com.intuisoft.plaid.databinding.FragmentWelcomeBinding
import com.intuisoft.plaid.features.homescreen.adapters.BasicWalletDataAdapter
import com.intuisoft.plaid.features.homescreen.viewmodel.HomeScreenViewModel
import com.intuisoft.plaid.features.onboarding.ui.AllSetFragmentDirections
import com.intuisoft.plaid.features.onboarding.viewmodel.OnboardingViewModel
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.features.pin.viewmodel.PinViewModel
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.repositories.LocalStoreRepository
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.walletmanager.ManagerState
import com.intuisoft.plaid.walletmanager.WalletManager
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class HomescreenFragment : PinProtectedFragment<FragmentHomescreenBinding>() {
    protected val viewModel: HomeScreenViewModel by sharedViewModel()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onBackPressedCallback {
            requireActivity().finish()
        }

        viewModel.updateGreeting()
        viewModel.showWallets()

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
            findNavController().navigate(
                HomescreenFragmentDirections.actionHomescreenFragmentToSettingsFragment(),
                Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION
            )
        }

        binding.addWallet.setOnClickListener {
            findNavController().navigate(
                HomescreenFragmentDirections.actionHomescreenFragmentToCreateWalletFragment(),
                Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION
            )
        }
    }

    fun onWalletSelected(wallet: LocalWalletModel) {


        val bundle = bundleOf(Constants.Navigation.WALLET_NAME_BUNDLE_ID to wallet.name)
        findNavController().navigate(
            R.id.wallet_dashboard_flow_graph,
            bundle,
            Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION
        )
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
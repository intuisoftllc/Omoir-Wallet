package com.intuisoft.plaid.features.dashboardscreen.ui

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
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.transition.TransitionInflater
import com.docformative.docformative.toArrayList
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.databinding.FragmentHomescreenBinding
import com.intuisoft.plaid.databinding.FragmentWalletDashboardBinding
import com.intuisoft.plaid.databinding.FragmentWelcomeBinding
import com.intuisoft.plaid.features.dashboardscreen.viewmodel.DashboardViewModel
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class DashboardFragment : PinProtectedFragment<FragmentWalletDashboardBinding>() {
    protected val viewModel: DashboardViewModel by sharedViewModel()
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: WalletManager by inject()


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

        viewModel.displayWallet.observe(viewLifecycleOwner, Observer { wallet ->
            binding.walletNameView.text = wallet.name
            binding.balance.text = wallet.getBalance(localStoreRepository)
            wallet.walletStateUpdated.observe(viewLifecycleOwner, Observer {
                wallet.onWalletStateChanged(binding.balance, it, localStoreRepository)
            })

            viewModel.getTransactions()
        })

        viewModel.transactions.observe(viewLifecycleOwner, Observer {
            binding.noTransactionsIcon.isVisible = it.isEmpty()
            binding.noTransactionsMessage.isVisible = it.isEmpty()
            binding.transactions.isVisible = it.isNotEmpty()
        })

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }
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
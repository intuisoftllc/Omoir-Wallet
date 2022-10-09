package com.intuisoft.plaid.features.createwallet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.SettingsItemView
import com.intuisoft.plaid.databinding.FragmentCreateImportWalletBinding
import com.intuisoft.plaid.features.createwallet.ZoomOutPageTransformer
import com.intuisoft.plaid.features.createwallet.adapters.WalletBenefitsAdapter
import com.intuisoft.plaid.features.createwallet.viewmodel.CreateWalletViewModel
import com.intuisoft.plaid.features.homescreen.ui.HomescreenFragmentDirections
import com.intuisoft.plaid.features.pin.ui.PinProtectedFragment
import com.intuisoft.plaid.util.Constants
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class CreateWalletFragment : PinProtectedFragment<FragmentCreateImportWalletBinding>() {
    protected val viewModel: CreateWalletViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCreateImportWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setUseTestNet(false)
        binding.advancedOptions.onClick {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(com.intuisoft.plaid.R.layout.create_import_advanced_options)
            val mainNet = bottomSheetDialog.findViewById<SettingsItemView>(R.id.mainNetOption)
            val testNet = bottomSheetDialog.findViewById<SettingsItemView>(R.id.testNetOption)

            testNet?.showCheck(viewModel.useTestNet)
            mainNet?.showCheck(!viewModel.useTestNet)

            mainNet?.onClick {
                viewModel.setUseTestNet(false)
                testNet?.showCheck(false)
                mainNet.showCheck(true)
            }

            testNet?.onClick {
                viewModel.setUseTestNet(true)
                testNet.showCheck(true)
                mainNet?.showCheck(false)
            }

            bottomSheetDialog.show()
        }

        val adapter = WalletBenefitsAdapter(
            requireActivity()
        )

        binding.createNewWallet.onClick {
            findNavController().navigate(
                CreateWalletFragmentDirections.actionCreateWalletFragmentToBackupWalletFragment(),
                Constants.Navigation.ANIMATED_ENTER_EXIT_RIGHT_NAV_OPTION
            )
        }

        binding.useCasesViewpager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.useCasesViewpager) { tab, position ->
        }.attach()

        binding.useCasesViewpager.setPageTransformer(ZoomOutPageTransformer())
    }

    override fun showActionBar(): Boolean {
        return false
    }

    override fun actionBarTitle(): Int {
        return 0
    }

    override fun navigationId(): Int {
        return R.id.createWalletFragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
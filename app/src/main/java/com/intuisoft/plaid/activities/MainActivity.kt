package com.intuisoft.plaid.activities

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.model.DevicePerformanceLevel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.ActivityMainBinding
import com.intuisoft.plaid.features.splash.ui.SplashFragment
import com.intuisoft.plaid.listeners.BarcodeResultListener
import com.intuisoft.plaid.listeners.NetworkStateChangeListener
import com.intuisoft.plaid.recievers.NetworkChangeReceiver
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import org.koin.android.ext.android.inject


class MainActivity : BindingActivity<ActivityMainBinding>(), ActionBarDelegate {
    lateinit var receiver: NetworkChangeReceiver
    lateinit var intentFilter: IntentFilter
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()
    var configurationSetup = false

    companion object {
        private val TOP_LEVEL_DESTINATIONS = setOf(
            R.id.homescreenFragment
        )

        private val TOP_LEVEL_BOTTOM_BAR_DESTINATIONS = setOf(
            R.id.walletDashboardFragment,
            R.id.swapFragment,
            R.id.marketFragment
        )

    }

    private val barcodeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val bitcoinAddress = result.data?.getStringExtra(Constants.ActivityResult.BARCODE_EXTRA)
                bitcoinAddress?.let {
                    val listener = supportFragmentManager.currentNavigationFragment as? BarcodeResultListener
                    listener?.onAddressReceived(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        receiver = NetworkChangeReceiver() {
            val listener = supportFragmentManager.currentNavigationFragment as? NetworkStateChangeListener

            if(supportFragmentManager.currentNavigationFragment !is SplashFragment) {
                listener?.onNetworkStateChanged(it)
            }
        }

        intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
    }

    fun activateAnimatedLoading(activate: Boolean, message: String) {
        binding.animatedLoadingContainer.isVisible = activate
        binding.loadingMessage.text = message
        binding.noInternet.isVisible = false
        binding.contentUnavailable.isVisible = false
    }

    fun activateNoInternet(activate: Boolean) {
        binding.noInternet.isVisible = activate
        binding.animatedLoadingContainer.isVisible = false
        binding.contentUnavailable.isVisible = false
    }

    fun activateContentUnavailable(activate: Boolean, message: String) {
        binding.contentUnavailable.isVisible = activate
        binding.contentUnavailableMessage.text = message
        binding.noInternet.isVisible = false
        binding.animatedLoadingContainer.isVisible = false
    }

    fun performSetup() {
        if(!configurationSetup) {
            configurationSetup = true
            setupPerformanceLevel()
            setupBottomNavigationBar()
        }
    }

    private fun setupPerformanceLevel() {
        if(localStoreRepository.getDevicePerformanceLevel() == null) {
            when {
                (application as PlaidApp).devicePerformance.mediaPerformanceClass >= Build.VERSION_CODES.S -> {
                    localStoreRepository.setDevicePerformanceLevel(DevicePerformanceLevel.HIGH)
                    // Performance class level 12 and above
                    // Provide the most premium experience for highest performing devices
                }
                (application as PlaidApp).devicePerformance.mediaPerformanceClass == Build.VERSION_CODES.R -> {
                    localStoreRepository.setDevicePerformanceLevel(DevicePerformanceLevel.MED)
                    // Performance class level 11
                    // Provide a high quality experience
                }
                else -> {
                    localStoreRepository.setDevicePerformanceLevel(DevicePerformanceLevel.DEFAULT)
                    // Performance class level undefined
                }
            }
        }
    }

    private fun setupBottomNavigationBar() {
        val navController = getNavController()

        if (localStoreRepository.isProEnabled()) {
            // todo
        } else {

        }


        binding.bottomBar.setConfiguration(
            getString(R.string.wallet),
            R.drawable.ic_bottom_bar_wallet_selected,
            R.drawable.ic_bottom_bar_wallet_unselected,
            "",
            0,
            0,
            R.drawable.ic_bottom_bar_swap_selected,
            R.drawable.ic_bottom_bar_swap_unselected,
            getString(R.string.market),
            R.drawable.ic_bottom_bar_market_selected,
            R.drawable.ic_bottom_bar_market_unselected,
            "",
            0,
            0,
            R.color.brand_color_dark_blue,
            R.color.text_grey
        )

        binding.bottomBar.setupDestinations(
            R.id.walletDashboardFragment,
            0,
            R.id.swapFragment,
            R.id.marketFragment,
            0
        )

        binding.bottomBar.onItemClicked { destination ->
            val delegate = supportFragmentManager.currentNavigationFragment as? FragmentBottomBarBarDelegate
            delegate?.let { fragment ->
                if(delegate.navigationId() != destination && destination != 0) {
                    delegate.onNavigateTo(destination)
                }
            }
        }

        setBottomNavVisibility(navController)
    }

    private fun setBottomNavVisibility(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomBar.onDestinationChanged(destination.id)
            binding.bottomBar.isVisible = TOP_LEVEL_BOTTOM_BAR_DESTINATIONS.contains(destination.id)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    fun scanBarcode() {
        BarcodeScannerActivity.invoiceMode = false
        barcodeLauncher.launch(Intent(this, BarcodeScannerActivity::class.java))
    }

    fun scanInvoice() {
        BarcodeScannerActivity.invoiceMode = true
        barcodeLauncher.launch(Intent(this, BarcodeScannerActivity::class.java))
    }

    override val isActionBarShowing: Boolean
        get() = binding.toolbar?.variant != TopBarView.NO_BAR

    override fun setActionBarTitle(title: String) {
        binding.toolbar.setPrimaryText(title)
    }

    override fun setActionBarSubTitle(title: String) {
        binding.toolbar.setSecondaryText(title)
    }

    override fun setActionBarActionLeft(@DrawableRes action: Int) {
        binding.toolbar.setActionLeft(action)
    }

    override fun setActionBarActionRight(@DrawableRes action: Int) {
        binding.toolbar.setActionRight(action)
    }

    override fun setActionBarVariant(variant: Int) {
        binding.toolbar.setBarStyle(variant)

        if(variant == TopBarView.CENTER_ALIGN_WHITE) {
            isLightStatusBar = true
            statusBarColor = getColor(R.color.background_color)
        } else {
            isLightStatusBar = false
            statusBarColor = getColor(R.color.brand_color_dark_blue)
        }

        binding.toolbar.setOnActionLeftClick {
            if(supportFragmentManager.currentNavigationFragment is FragmentActionBarDelegate) {
                (supportFragmentManager.currentNavigationFragment as? FragmentActionBarDelegate)
                    ?.onActionLeft()
            }
        }

        binding.toolbar.setOnActionRightClick {
            if(supportFragmentManager.currentNavigationFragment is FragmentActionBarDelegate) {
                (supportFragmentManager.currentNavigationFragment as? FragmentActionBarDelegate)
                    ?.onActionRight()
            }
        }

        binding.toolbar.setSecondaryTextOnClick {
            if(supportFragmentManager.currentNavigationFragment is FragmentActionBarDelegate) {
                (supportFragmentManager.currentNavigationFragment as? FragmentActionBarDelegate)
                    ?.onSubtitleClicked()
            }
        }
    }



    private fun getNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        return navHostFragment.navController
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val appBarConfiguration = AppBarConfiguration(
            TOP_LEVEL_DESTINATIONS
        )

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
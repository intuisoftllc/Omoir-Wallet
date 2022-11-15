package com.intuisoft.plaid.activities

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
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
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.databinding.ActivityMainBinding
import com.intuisoft.plaid.features.splash.ui.SplashFragment
import com.intuisoft.plaid.listeners.BarcodeResultListener
import com.intuisoft.plaid.listeners.NetworkStateChangeListener
import com.intuisoft.plaid.recievers.NetworkChangeReceiver
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import com.intuisoft.plaid.walletmanager.WalletManager
import org.koin.android.ext.android.inject


class MainActivity : BindingActivity<ActivityMainBinding>(), ActionBarDelegate {
    lateinit var receiver: NetworkChangeReceiver
    lateinit var intentFilter: IntentFilter
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()

    companion object {
        private val TOP_LEVEL_DESTINATIONS = setOf(
            R.id.homescreenFragment
        )

        private val TOP_LEVEL_BOTTOM_BAR_DESTINATIONS = setOf(
            R.id.walletDashboardFragment,
            R.id.swapFragment
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
                listener?.onStateChanged(it)
            }
        }

        intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        val navController = getNavController()

        if(localStoreRepository.isProEnabled()) {

        } else {
            binding.bottomBar.setConfiguration(
                getString(R.string.wallet), R.drawable.ic_bottom_bar_wallet_selected, R.drawable.ic_bottom_bar_wallet_unselected,
                "", 0, 0,
                R.drawable.ic_bottom_bar_swap_selected, R.drawable.ic_bottom_bar_swap_unselected,
                getString(R.string.market), R.drawable.ic_bottom_bar_market_selected, R.drawable.ic_bottom_bar_market_unselected,
                "", 0, 0,
                R.color.brand_color_dark_blue, R.color.text_grey
            )

            binding.bottomBar.setupDestinations(
                R.id.walletDashboardFragment,
                0,
                R.id.swapFragment,
                0, // todo: market fragment
                0
            )
        }

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
        registerReceiver(receiver, intentFilter);
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    fun scanBarcode() {
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
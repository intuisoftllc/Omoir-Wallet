package com.intuisoft.plaid.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.google.gson.Gson
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.*
import com.intuisoft.plaid.androidwrappers.PasscodeView.PasscodeViewType.Companion.TYPE_CHECK_PASSCODE
import com.intuisoft.plaid.androidwrappers.delegates.ActionBarDelegate
import com.intuisoft.plaid.androidwrappers.delegates.FragmentActionBarDelegate
import com.intuisoft.plaid.androidwrappers.delegates.FragmentBottomBarBarDelegate
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.coroutines.PlaidScope
import com.intuisoft.plaid.common.model.AppTheme
import com.intuisoft.plaid.common.model.HiddenWalletModel
import com.intuisoft.plaid.common.model.StoredHiddenWalletsModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Navigation.PASSPHRASES
import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.common.util.extensions.safeWalletScope
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.ActivityMainBinding
import com.intuisoft.plaid.features.splash.ui.SplashFragment
import com.intuisoft.plaid.listeners.BarcodeResultListener
import com.intuisoft.plaid.listeners.NetworkStateChangeListener
import com.intuisoft.plaid.recievers.NetworkChangeReceiver
import com.intuisoft.plaid.walletmanager.AbstractWalletManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MainActivity : BindingActivity<ActivityMainBinding>(), ActionBarDelegate {
    lateinit var receiver: NetworkChangeReceiver
    lateinit var intentFilter: IntentFilter
    protected val localStoreRepository: LocalStoreRepository by inject()
    protected val walletManager: AbstractWalletManager by inject()
    private var configurationSetup = false
    private val dialogStack = mutableListOf<Pair<AppCompatDialog, (() -> Unit)?>>()

    companion object {
        private val TOP_LEVEL_DESTINATIONS = setOf(
            R.id.homescreenFragment,
            R.id.proHomescreenFragment
        )

        private val IGNORE_BACK_PRESSED_DESTINATIONS = setOf(
            R.id.splashFragment,
            R.id.welcomeFragment,
            R.id.allSetFragment
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

        receiver = NetworkChangeReceiver {
            val listener = supportFragmentManager.currentNavigationFragment as? NetworkStateChangeListener

            if(supportFragmentManager.currentNavigationFragment !is SplashFragment) {
                listener?.onNetworkStateChanged(it)
            }
        }

        if (savedInstanceState == null) {
            val extras = intent.extras
            val passphrases: String? = extras?.getString(PASSPHRASES)
            passphrases?.let {
                val list: List<Pair<String, HiddenWalletModel?>> = Gson().fromJson(it, StoredHiddenWalletsModel::class.java).hiddenWallets
                val map = mutableMapOf<String, HiddenWalletModel?>()
                list.forEach {
                    map.put(it.first, it.second)
                }

                walletManager.setInitialHiddenWallets(map)
            }
        }

        intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val listener = supportFragmentManager.currentNavigationFragment as? FragmentBottomBarBarDelegate

                    if(!IGNORE_BACK_PRESSED_DESTINATIONS.contains(listener?.navigationId() ?: 0)
                        && !binding.pin.isVisible) {
                        listener?.onBackPressed()
                    }
                }
            })
    }

    fun setAppTheme() {
        CommonService.getAppPrefs().let {
            when (it.appTheme) {
                AppTheme.AUTO -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }

                AppTheme.LIGHT -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

                AppTheme.DARK -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
    }

    fun addToDialogStack(dialog: AppCompatDialog, onCancel: (() -> Unit)? = null) {
        if(dialogStack.find { it === dialog } == null) {
            dialogStack.add(dialog to onCancel)
        }
    }

    fun removeFromDialogStack(dialog: AppCompatDialog) {
        dialogStack.remove {
            it == dialog
        }
    }

    fun clearDialogStack() {
        dialogStack.toArrayList()
            .forEach {
                it.second?.invoke()
                it.first.cancel()
            }
        dialogStack.clear()
    }

    fun hidePinIfNeeded() {
        if(binding.pin.isUnlocked) {
            binding.pin.animateDown {
                binding.pin.isVisible = false
                binding.pin.translationY = 0f
            }
        }
    }

    fun activatePin(setupPin: Boolean, loadingUserData: Boolean) {
        if(binding.pin.isVisible) return

        var SetupPin = setupPin
        binding.pin.isVisible = true
        binding.pin.setPasscodeType(TYPE_CHECK_PASSCODE)
        if(SetupPin) {
            binding.pin.setFirstInputTip(getString(R.string.enter_pin_to_reset_message))
            binding.pin.resetView()
        } else {
            binding.pin.setFirstInputTip(getString(R.string.enter_pin_to_unlock_message))
            binding.pin.resetView()
        }

        if(SetupPin || loadingUserData || CommonService.getUserPin().isEmpty()) {
            binding.pin.disableFingerprint()
        }

        binding.pin.setListener(object: PasscodeView.PasscodeViewListener {
            override fun onFail(wrongNumber: String?) {
                // do nothing
            }

            override fun onSuccess(number: String?) {
                localStoreRepository.updatePinCheckedTime()

                if(SetupPin) {
                    SetupPin = false
                    binding.pin.setPasscodeType(PasscodeView.PasscodeViewType.TYPE_SET_PASSCODE)
                    binding.pin.setFirstInputTip(getString(R.string.create_pin_tip_message))
                    binding.pin.setSecondInputTip(getString(R.string.re_enter_pin_tip_message))
                    binding.pin.resetView()
                    binding.pin.disablePinAttemptTracking()
                } else {
                    walletManager.start()

                    if(loadingUserData) {
                        if(localStoreRepository.isProEnabled()) {
                            getNavController().navigate(R.id.proHomescreenFragment, null, Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION)
                        } else {
                            getNavController().navigate(R.id.homescreenFragment, null, Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION)
                        }
                    } else hidePinIfNeeded() // only hide if a frag is showing or after it has been shown
                }
            }

            override fun onMaxAttempts() {
                val progressDialog = ProgressDialog.show(this@MainActivity, getString(R.string.wiping_data_title), getString(R.string.wiping_data_message))
                progressDialog.setCancelable(false)

                PlaidScope.IoScope.launch {
                    localStoreRepository.wipeAllData {
                        PlaidScope.MainScope.launch {
                            safeWalletScope {
                                progressDialog.cancel()

                                getNavController().navigate(
                                    R.id.splashFragment
                                )

                                binding.pin.isVisible = false
                            }
                        }
                    }
                }
            }

            override fun onScanFingerprint(listener: FingerprintScanResponse) {
                supportFragmentManager.currentNavigationFragment!!.validateFingerprint(
                    onSuccess = {
                        binding.pin.setUnlocked()
                        listener.onScanSuccess()
                    },

                    onError = {
                        listener.onScanFail()
                    },

                    subTitle = Constants.Strings.USE_BIOMETRIC_REASON_2,
                    negativeText = Constants.Strings.USE_PIN
                )
            }

        })
    }

    fun showBottomBar(show: Boolean) {
        binding.bottomBar.isVisible = show
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
            setupBottomNavigationBar()
        }
    }

    private fun setupBottomNavigationBar() {
        val navController = getNavController()

        if (localStoreRepository.isProEnabled()) {
            binding.bottomBar.setConfiguration(
                getString(R.string.wallet),
                R.drawable.ic_bottom_bar_wallet_selected,
                R.drawable.ic_bottom_bar_wallet_unselected,
                getString(R.string.market),
                R.drawable.ic_bottom_bar_market_selected,
                R.drawable.ic_bottom_bar_market_unselected,
                R.drawable.ic_bottom_bar_swap_selected,
                R.drawable.ic_bottom_bar_swap_unselected,
                getString(R.string.atp),
                R.drawable.ic_atp_selected,
                R.drawable.ic_atp_unselected,
                getString(R.string.reports),
                R.drawable.ic_reports_selected,
                R.drawable.ic_reports_unselected,
                R.color.brand_color_dark_blue,
                R.color.subtitle_text_color
            )

            binding.bottomBar.setupDestinations(
                R.id.walletProDashboardFragment,
                R.id.marketFragment,
                R.id.exchangeFragment,
                R.id.atpFragment,
                R.id.reportsFragment
            )
        } else {
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
                R.color.subtitle_text_color
            )

            binding.bottomBar.setupDestinations(
                R.id.walletDashboardFragment,
                0,
                R.id.exchangeFragment,
                R.id.marketFragment,
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
        (application as PlaidApp).ignorePinCheck = true
        BarcodeScannerActivity.invoiceMode = false
        barcodeLauncher.launch(Intent(this, BarcodeScannerActivity::class.java))
    }

    fun scanInvoice() {
        (application as PlaidApp).ignorePinCheck = true
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

        baseContext.doOnUiMode(
            onNightMode = {
                isLightStatusBar = false
            }
        )

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
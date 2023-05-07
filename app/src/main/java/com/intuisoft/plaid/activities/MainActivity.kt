package com.intuisoft.plaid.activities

import android.app.Activity
import android.app.Dialog
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
import com.intuisoft.plaid.billing.BillingManager
import com.intuisoft.plaid.common.CommonService
import com.intuisoft.plaid.common.coroutines.PlaidScope
import com.intuisoft.plaid.common.model.AppTheme
import com.intuisoft.plaid.common.model.HiddenWalletModel
import com.intuisoft.plaid.common.model.StoredHiddenWalletsModel
import com.intuisoft.plaid.common.repositories.LocalStoreRepository
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.common.util.Constants.Navigation.PASSPHRASES
import com.intuisoft.plaid.common.util.extensions.remove
import com.intuisoft.plaid.common.util.extensions.toArrayList
import com.intuisoft.plaid.databinding.ActivityMainBinding
import com.intuisoft.plaid.features.splash.ui.SplashFragment
import com.intuisoft.plaid.listeners.BarcodeResultListener
import com.intuisoft.plaid.listeners.NetworkStateChangeListener
import com.intuisoft.plaid.recievers.NetworkChangeReceiver
import com.intuisoft.plaid.common.delegates.wallet.WalletDelegate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MainActivity : BindingActivity<ActivityMainBinding>(), ActionBarDelegate {
    lateinit var receiver: NetworkChangeReceiver
    lateinit var intentFilter: IntentFilter
    protected val localStoreRepository: LocalStoreRepository by inject()
//    protected val billingManager: BillingManager by inject()
    protected val walletManager: WalletDelegate by inject()
    protected val billing: BillingManager by inject()
    private var configurationSetup = false
    private var backAllowed = false
    private val dialogStack = mutableListOf<Pair<Dialog, (() -> Unit)?>>()

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
                    withBinding {
                        val listener =
                            supportFragmentManager.currentNavigationFragment as? FragmentBottomBarBarDelegate

                        if (
                            !IGNORE_BACK_PRESSED_DESTINATIONS.contains(listener?.navigationId() ?: 0)
                            && !pin.isVisible
                        ) {
                            listener?.onBackPressed()
                        } else if(pin.isVisible && backAllowed) {
                            hidePinIfNeeded()
                        }
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

    fun addToDialogStack(dialog: Dialog, onCancel: (() -> Unit)? = null) {
        if(dialogStack.find { it === dialog } == null) {
            dialogStack.add(dialog to onCancel)
        }
    }

    fun removeFromDialogStack(dialog: Dialog) {
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
        withBinding {
            if(pin.isVisible && (pin.isUnlocked || backAllowed)) {
                backAllowed = false
                pin.animateDown {
                    pin.isVisible = false
                    pin.translationY = 0f
                }
            }
        }
    }

    fun activatePin(setupPin: Boolean, loadingUserData: Boolean) {
        withBinding {
            if (pin.isVisible) return@withBinding

            var SetupPin = setupPin
            backAllowed = SetupPin
            pin.isVisible = true
            pin.setPasscodeType(TYPE_CHECK_PASSCODE)
            pin.enablePinAttemptTracking(true)
            if (SetupPin) {
                pin.setFirstInputTip(getString(R.string.enter_pin_to_reset_message))
                pin.resetView()
            } else {
                pin.setFirstInputTip(getString(R.string.enter_pin_to_unlock_message))
                pin.resetView()
            }

            if (SetupPin || loadingUserData || CommonService.getUserPin().isEmpty()) {
                pin.disableFingerprint()
            }

            pin.setListener(object : PasscodeView.PasscodeViewListener {
                override fun onFail(wrongNumber: String?) {
                    // do nothing
                }

                override fun onSuccess(number: String?) {
                    if (!pin.isVisible) return
                    localStoreRepository.updatePinCheckedTime()

                    if (SetupPin) {
                        SetupPin = false
                        pin.setPasscodeType(PasscodeView.PasscodeViewType.TYPE_SET_PASSCODE)
                        pin.setFirstInputTip(getString(R.string.create_pin_tip_message))
                        pin.setSecondInputTip(getString(R.string.re_enter_pin_tip_message))
                        pin.resetView()
                        pin.enablePinAttemptTracking(false)
                    } else {
                        walletManager.start()

                        if (loadingUserData) {
                            billing.shouldShowPremiumContent { show ->
                                if(show) {
                                    getNavController().navigate(
                                        R.id.proHomescreenFragment,
                                        null,
                                        Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION
                                    )
                                } else {
                                    getNavController().navigate(
                                        R.id.homescreenFragment,
                                        null,
                                        Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION
                                    )
                                }
                            }
                        } else hidePinIfNeeded() // only hide if a frag is showing or after it has been shown
                    }
                }

                override fun onMaxAttempts() {
                    val progressDialog = ProgressDialog.show(
                        this@MainActivity,
                        getString(R.string.wiping_data_title),
                        getString(R.string.wiping_data_message)
                    )
                    progressDialog.setCancelable(false)

                    PlaidScope.applicationScope.launch(Dispatchers.IO) {
                        localStoreRepository.wipeAllData {
                            PlaidScope.MainScope.launch {
                                progressDialog.cancel()
                                pin.isVisible = false
                                softRestart(walletManager, localStoreRepository)
                            }
                        }
                    }
                }

                override fun onScanFingerprint(listener: FingerprintScanResponse) {
                    supportFragmentManager.currentNavigationFragment!!.validateFingerprint(
                        onSuccess = {
                            pin.setUnlocked()
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
    }

    fun showBottomBar(show: Boolean) {
        withBinding {
            bottomBar.isVisible = show
        }
    }

    fun activateAnimatedLoading(activate: Boolean, message: String) {
        withBinding {
            animatedLoadingContainer.isVisible = activate
            loadingMessage.text = message
            noInternet.isVisible = false
            contentUnavailable.isVisible = false
        }
    }

    fun activateNoInternet(activate: Boolean) {
        withBinding {
            noInternet.isVisible = activate
            animatedLoadingContainer.isVisible = false
            contentUnavailable.isVisible = false
        }
    }

    fun activateContentUnavailable(activate: Boolean, message: String) {
        withBinding {
            contentUnavailable.isVisible = activate
            contentUnavailableMessage.text = message
            noInternet.isVisible = false
            animatedLoadingContainer.isVisible = false
        }
    }

    fun performSetup() {
        if(!configurationSetup) {
            configurationSetup = true
            setupBottomNavigationBar()
        }
    }

    private fun setupBottomNavigationBar() {
        withBinding {
            val navController = getNavController()

            if (localStoreRepository.isPremiumUser()) {
                bottomBar.setConfiguration(
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

                bottomBar.setupDestinations(
                    R.id.walletProDashboardFragment,
                    R.id.marketFragment,
                    R.id.exchangeFragment,
                    R.id.atpFragment,
                    R.id.reportsFragment
                )
            } else {
                bottomBar.setConfiguration(
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

                bottomBar.setupDestinations(
                    R.id.walletDashboardFragment,
                    0,
                    R.id.exchangeFragment,
                    R.id.marketFragment,
                    0
                )
            }


            bottomBar.onItemClicked { destination ->
                val delegate =
                    supportFragmentManager.currentNavigationFragment as? FragmentBottomBarBarDelegate
                delegate?.let { fragment ->
                    if (delegate.navigationId() != destination && destination != 0) {
                        delegate.onNavigateTo(destination)
                    }
                }
            }

            setBottomNavVisibility(navController)
        }
    }

    private fun setBottomNavVisibility(navController: NavController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            withBinding {
                bottomBar.onDestinationChanged(destination.id)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
//        billingManager.stop()
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
        withBinding {
            toolbar.setPrimaryText(title)
        }
    }

    override fun setActionBarSubTitle(title: String) {
        withBinding {
            toolbar.setSecondaryText(title)
        }
    }

    override fun setActionBarActionLeft(@DrawableRes action: Int) {
        withBinding {
            toolbar.setActionLeft(action)
        }
    }

    override fun setActionBarActionRight(@DrawableRes action: Int) {
        withBinding {
            toolbar.setActionRight(action)
        }
    }

    override fun setActionBarVariant(variant: Int) {
        withBinding {
            toolbar.setBarStyle(variant)

            if (variant == TopBarView.CENTER_ALIGN_WHITE) {
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

            toolbar.setOnActionLeftClick {
                if (supportFragmentManager.currentNavigationFragment is FragmentActionBarDelegate) {
                    (supportFragmentManager.currentNavigationFragment as? FragmentActionBarDelegate)
                        ?.onActionLeft()
                }
            }

            toolbar.setOnActionRightClick {
                if (supportFragmentManager.currentNavigationFragment is FragmentActionBarDelegate) {
                    (supportFragmentManager.currentNavigationFragment as? FragmentActionBarDelegate)
                        ?.onActionRight()
                }
            }

            toolbar.setSecondaryTextOnClick {
                if (supportFragmentManager.currentNavigationFragment is FragmentActionBarDelegate) {
                    (supportFragmentManager.currentNavigationFragment as? FragmentActionBarDelegate)
                        ?.onSubtitleClicked()
                }
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
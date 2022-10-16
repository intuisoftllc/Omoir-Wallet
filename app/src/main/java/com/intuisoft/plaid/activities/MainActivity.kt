package com.intuisoft.plaid.activities

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.intuisoft.plaid.R
import com.intuisoft.plaid.androidwrappers.ActionBarDelegate
import com.intuisoft.plaid.androidwrappers.BindingActivity
import com.intuisoft.plaid.androidwrappers.currentNavigationFragment
import com.intuisoft.plaid.databinding.ActivityMainBinding
import com.intuisoft.plaid.features.splash.ui.SplashFragment
import com.intuisoft.plaid.listeners.BarcodeResultListener
import com.intuisoft.plaid.listeners.NetworkStateChangeListener
import com.intuisoft.plaid.recievers.NetworkChangeReceiver
import com.intuisoft.plaid.util.Constants


class MainActivity : BindingActivity<ActivityMainBinding>(), ActionBarDelegate {

    private lateinit var appBarConfiguration: AppBarConfiguration
    lateinit var receiver: NetworkChangeReceiver
    lateinit var intentFilter: IntentFilter

    companion object {
        private val TOP_LEVEL_DESTINATIONS = setOf(
            R.id.homescreenFragment
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
        setSupportActionBar(binding.toolbar)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setDisplayShowHomeEnabled(true);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        receiver = NetworkChangeReceiver() {
            val listener = supportFragmentManager.currentNavigationFragment as? NetworkStateChangeListener

            if(supportFragmentManager.currentNavigationFragment !is SplashFragment) {
                listener?.onStateChanged(it)
            }
        }

        intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
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

    override var isActionBarShowing: Boolean
        get() = supportActionBar?.isShowing ?: false
        set(value) {
            if (value) {
                supportActionBar?.show()
            } else {
                supportActionBar?.hide()
            }
        }

    override var actionBarTitle: CharSequence?
        get() = supportActionBar?.title
        set(value) {
            supportActionBar?.title = value
        }

    override fun showActionBarTitle() {
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    override fun hideActionBarTitle() {
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun setActionBarTitle(@StringRes title: Int) {
        supportActionBar?.setTitle(title)
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
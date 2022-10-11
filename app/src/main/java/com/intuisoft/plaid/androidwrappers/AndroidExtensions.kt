package com.intuisoft.plaid.androidwrappers

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.IdRes
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.intuisoft.plaid.MainActivity
import com.intuisoft.plaid.NavGraphDirections
import com.intuisoft.plaid.R
import com.intuisoft.plaid.features.settings.ui.SettingsFragmentDirections
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.util.Constants
import com.intuisoft.plaid.util.entensions.getColorFromAttr
import okhttp3.internal.concurrent.TaskRunner.Companion.logger
import okhttp3.internal.http2.Http2Reader.Companion.logger
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ViewModelParameter
import org.koin.androidx.viewmodel.scope.BundleDefinition
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import java.util.concurrent.Executor

val Activity.windowInsetsController: WindowInsetsControllerCompat
    get() = WindowInsetsControllerCompat(window, window.decorView)

var Activity.isLightStatusBar: Boolean
    get() = windowInsetsController.isAppearanceLightStatusBars
    set(value) {
        windowInsetsController.isAppearanceLightStatusBars = value
    }

var Activity.statusBarColor: Int
    get() = window.statusBarColor
    set(value) {
        window.statusBarColor = value
    }

val Fragment.mainActivity: MainActivity
    get() = requireActivity() as MainActivity

fun Fragment.longToast(message: String) =
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()

fun Fragment.shortToast(message: String) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

// extension function to hide soft keyboard programmatically
fun Activity.hideSoftKeyboard(){
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}

fun Fragment.ignoreOnBackPressed() {
    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // ignore back presses
            }
        })
}

fun Fragment.onBackPressedCallback(onBackPressed: () -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback ( viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
    )
}


fun Fragment.validateFingerprint(
    title: String = Constants.Strings.USE_BIOMETRIC_AUTH,
    subTitle: String = Constants.Strings.USE_BIOMETRIC_REASON_1,
    negativeText: String = Constants.Strings.SKIP_FOR_NOW,
    onFail: (() -> Unit)? = null,
    onError: (() -> Unit)? = null,
    onSuccess: () -> Unit
) {
    var executor: Executor = ContextCompat.getMainExecutor(requireContext())
    var biometricPrompt = BiometricPrompt(this, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int,
                                               errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError?.invoke()
            }

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFail?.invoke()
            }
        })

    var promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setDescription(subTitle)
        .setNegativeButtonText(negativeText)
        .build()

    biometricPrompt.authenticate(promptInfo)
}

fun styledSnackBar(root: View, title: String, onDismissed: (() -> Unit)? = null) {
    Snackbar.make(root, title, Snackbar.LENGTH_LONG)
        .setBackgroundTint(root.context.getColorFromAttr(com.google.android.material.R.attr.colorSecondary))
        .setActionTextColor(root.context.getColorFromAttr(com.google.android.material.R.attr.colorOnSecondary))
        .addCallback(object : Snackbar.Callback() {

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                onDismissed?.invoke()
            }
        })
        .show()
}

fun Fragment.navigate(navId: Int, wallet: LocalWalletModel, options: NavOptions = Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION) {
    val bundle = bundleOf(Constants.Navigation.WALLET_UUID_BUNDLE_ID to wallet.uuid)
    findNavController().navigate(
        navId,
        bundle,
        options
    )
}

fun Fragment.navigate(navId: Int, uuid: String, options: NavOptions = Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION) {
    val bundle = bundleOf(Constants.Navigation.WALLET_UUID_BUNDLE_ID to uuid)
    findNavController().navigate(
        navId,
        bundle,
        options
    )
}

fun Fragment.navigate(navId: Int, options: NavOptions = Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION) {
    findNavController().navigate(
        navId,
        null,
        options
    )
}

fun Fragment.navigate(navId: Int, bundle: Bundle) {
    findNavController().navigate(
        navId,
        bundle,
        null
    )
}

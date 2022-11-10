package com.intuisoft.plaid.androidwrappers

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.R
import com.google.android.material.snackbar.Snackbar
import com.intuisoft.plaid.activities.MainActivity
import com.intuisoft.plaid.model.LocalWalletModel
import com.intuisoft.plaid.common.util.Constants
import com.intuisoft.plaid.util.entensions.getColorFromAttr
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

val FragmentManager.currentNavigationFragment: Fragment?
    get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()

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
    title: String = com.intuisoft.plaid.common.util.Constants.Strings.USE_BIOMETRIC_AUTH,
    subTitle: String = com.intuisoft.plaid.common.util.Constants.Strings.USE_BIOMETRIC_REASON_1,
    negativeText: String = com.intuisoft.plaid.common.util.Constants.Strings.SKIP_FOR_NOW,
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

fun styledSnackBar(root: View, title: String, showTop: Boolean? = null, onDismissed: (() -> Unit)? = null) {
    val snack = Snackbar.make(root, title, Snackbar.LENGTH_LONG)
        .setBackgroundTint(root.context.getColorFromAttr(com.google.android.material.R.attr.colorPrimary))
        .setActionTextColor(root.context.getColorFromAttr(com.google.android.material.R.attr.colorOnSecondary))
        .addCallback(object : Snackbar.Callback() {

            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                super.onDismissed(transientBottomBar, event)
                onDismissed?.invoke()
            }
        })

    showTop?.let {
        if(it) {
            val view = snack.view
            val params: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            params.topMargin = root.resources.dpToPixels(10f).toInt()
            view.layoutParams = params
        }
    }

    snack.view.findViewById<TextView>(R.id.snackbar_text).maxLines = 3
    snack.show()
}

fun Fragment.navigate(navId: Int, wallet: LocalWalletModel, options: NavOptions = com.intuisoft.plaid.common.util.Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION) {
    val bundle = bundleOf(com.intuisoft.plaid.common.util.Constants.Navigation.WALLET_UUID_BUNDLE_ID to wallet.uuid)
    findNavController().navigate(
        navId,
        bundle,
        options
    )
}

fun Fragment.navigate(navId: Int, uuid: String, options: NavOptions = com.intuisoft.plaid.common.util.Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION) {
    val bundle = bundleOf(com.intuisoft.plaid.common.util.Constants.Navigation.WALLET_UUID_BUNDLE_ID to uuid)
    findNavController().navigate(
        navId,
        bundle,
        options
    )
}

fun Fragment.navigate(navId: Int, options: NavOptions = com.intuisoft.plaid.common.util.Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION) {
    findNavController().navigate(
        navId,
        null,
        options
    )
}

fun Fragment.navigate(navId: Int, bundle: Bundle, options: NavOptions = com.intuisoft.plaid.common.util.Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION) {
    findNavController().navigate(
        navId,
        bundle,
        options
    )
}

fun Resources.dpToPixels(dp: Float) : Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        displayMetrics
    )
}

fun Context.copyToClipboard(text: CharSequence, textLabel: String){
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(textLabel, text)
    clipboard.setPrimaryClip(clip)
}

fun TextView.leftDrawable(@DrawableRes id: Int = 0, @DimenRes sizeRes: Int) {
    val drawable = ContextCompat.getDrawable(context, id)
    val size = resources.getDimensionPixelSize(sizeRes)
    drawable?.setBounds(0, 0, size, size)
    this.setCompoundDrawables(drawable, null, null, null)
}

fun ImageView.tint(color: Int) {
    this.drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
}

fun Button.tint(color: Int) {
    this.background.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
}

fun Activity.shareText(subject: String?, message: String) {
    val txtIntent = Intent(Intent.ACTION_SEND)
    txtIntent.type = "text/plain"
    subject?.let {
        txtIntent.putExtra(Intent.EXTRA_SUBJECT, it)
    }

    txtIntent.putExtra(Intent.EXTRA_TEXT, message)
    startActivity(Intent.createChooser(txtIntent, "Share"))
}

fun Activity.checkAppPermission(permission: String, requestCode: Int, onAlreadyGranted: () -> Unit) {
    if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
        // Requesting the permission
        requestPermissions(arrayOf(permission), requestCode)
    } else {
        onAlreadyGranted()
    }
}

fun Context.openLink(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}

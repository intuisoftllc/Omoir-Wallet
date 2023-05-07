package com.intuisoft.plaid.androidwrappers

import android.animation.ValueAnimator
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.google.android.material.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.intuisoft.plaid.PlaidApp
import com.intuisoft.plaid.activities.MainActivity
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

inline fun View.setOnSingleClickListener(
    minClickInterval: Int = Constants.Time.MIN_CLICK_INTERVAL_LONG,
    crossinline onClick: () -> Unit
) {
    setOnClickListener(
        object: SingleClickListener(minClickInterval) {
            override fun onSingleClick(v: View?) {
                onClick()
            }
        }
    )
}

// extension function to hide soft keyboard programmatically
fun Activity.hideSoftKeyboard(){
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
        hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}

fun Fragment.sendEmail(to: String, subject: String, message: String) {

    val emailIntent = Intent(Intent.ACTION_SEND);
    emailIntent.setType("text/plain");
    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    emailIntent.putExtra(
        Intent.EXTRA_TEXT,
        message
    )


    emailIntent.setType("message/rfc822")

    try {
        (requireActivity().application as PlaidApp).ignorePinCheck = true
        startActivity(
            Intent.createChooser(emailIntent, getString(com.intuisoft.plaid.R.string.settings_send_email_help_message)));
    } catch (ex: ActivityNotFoundException) {
        (requireActivity().application as PlaidApp).ignorePinCheck = false
        styledSnackBar(requireView(), getString(com.intuisoft.plaid.R.string.settings_send_email_error))
    }
}

fun Fragment.openLink(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("$url"))
    startActivity(browserIntent)
}

fun Context.doOnUiMode(
    onNightMode: (() -> Unit)? = null,
    onDayMode: (() -> Unit)? = null,
    onAutoMode: (() -> Unit)? = null
) {
    when (resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
        Configuration.UI_MODE_NIGHT_YES -> {
            onNightMode?.invoke()
        }
        Configuration.UI_MODE_NIGHT_NO -> {
            onDayMode?.invoke()
        }
        Configuration.UI_MODE_NIGHT_UNDEFINED -> {
            onAutoMode?.invoke()
        }
        else -> {
            onAutoMode?.invoke()
        }
    }
}

fun View.animateDown(duration: Long = 1000, onFinished: (() -> Unit)? = null) {
    val y = Resources.getSystem().getDisplayMetrics().heightPixels.toFloat()
    val valueAnimator = ValueAnimator.ofFloat(0f, y)
    valueAnimator.addUpdateListener {
        val value = it.animatedValue as Float
        translationY = value
        if(value == y) onFinished?.invoke()
    }

    valueAnimator.interpolator = LinearInterpolator()
    valueAnimator.duration = duration
    valueAnimator.start()
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

fun NavController.isFragmentInBackStack(destinationId: Int) =
    try {
        getBackStackEntry(destinationId)
        true
    } catch (e: Exception) {
        false
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

fun Fragment.navigate(navId: Int, options: NavOptions = Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION) {
    try {
        findNavController().navigate(
            navId,
            null,
            options
        )
    } catch(e: java.lang.IllegalStateException) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }
}

fun Fragment.navigate(navId: Int, bundle: Bundle, options: NavOptions = Constants.Navigation.ANIMATED_FADE_IN_EXIT_NAV_OPTION) {
    try {
        findNavController().navigate(
            navId,
            bundle,
            options
        )
    } catch(e: java.lang.IllegalStateException) {
        FirebaseCrashlytics.getInstance().recordException(e)
    }
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
    (application as PlaidApp).ignorePinCheck = true
    startActivity(Intent.createChooser(txtIntent, "Share"))
}

fun Activity.checkAppPermission(permission: String, requestCode: Int, onAlreadyGranted: () -> Unit) {
    if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
        // Requesting the permission
        (application as PlaidApp).ignorePinCheck = true
        requestPermissions(arrayOf(permission), requestCode)
    } else {
        onAlreadyGranted()
    }
}

fun Context.openLink(url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(browserIntent)
}

fun Context.openLink(uri: Uri) {
    val browserIntent = Intent(Intent.ACTION_VIEW, uri)
    startActivity(browserIntent)
}


fun Fragment.toast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
}

fun ImageView.loadUrl(url: String) {

    val imageLoader = ImageLoader.Builder(this.context)
        .componentRegistry { add(SvgDecoder(this@loadUrl.context)) }
        .build()

    val request = ImageRequest.Builder(this.context)
        .crossfade(true)
        .crossfade(500)
//        .placeholder(R.drawable.placeholder)
//        .error(R.drawable.error)
        .data(url)
        .target(this)
        .build()

    imageLoader.enqueue(request)
}
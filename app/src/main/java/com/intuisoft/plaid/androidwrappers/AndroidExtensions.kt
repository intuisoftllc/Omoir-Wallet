package com.intuisoft.plaid.androidwrappers

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.navigation.fragment.findNavController
import com.intuisoft.plaid.MainActivity
import okhttp3.internal.concurrent.TaskRunner.Companion.logger
import okhttp3.internal.http2.Http2Reader.Companion.logger
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.ViewModelParameter
import org.koin.androidx.viewmodel.scope.BundleDefinition
import org.koin.core.Koin
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier

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

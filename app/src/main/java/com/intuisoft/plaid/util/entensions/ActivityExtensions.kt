package com.intuisoft.plaid.util.entensions

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.intuisoft.plaid.util.Constants
import java.util.concurrent.Executor

// extension function to hide soft keyboard programmatically
fun Activity.hideSoftKeyboard(){
    (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
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


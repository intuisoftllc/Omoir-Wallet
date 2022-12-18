package com.intuisoft.plaid.androidwrappers

import android.view.View
import com.intuisoft.plaid.common.util.Constants

abstract class SingleClickListener(
    val minClickInterval: Int = Constants.Time.MIN_CLICK_INTERVAL_LONG
) : View.OnClickListener {

    /**
     * Last time view was clicked
     */
    private var mLastClickTime: Long = 0

    /**
     * @param v The view that was clicked.
     */
    abstract fun onSingleClick(v: View?)

    override fun onClick(v: View?) {
        val currentClickTime: Long = System.currentTimeMillis()
        val elapsedTime = currentClickTime - mLastClickTime
        mLastClickTime = currentClickTime
        if (elapsedTime <= minClickInterval) return
        onSingleClick(v)
    }


}
package com.intuisoft.plaid.androidwrappers

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.intuisoft.plaid.R
import kotlinx.android.synthetic.main.custom_view_chart_time_period_indicator.view.*

class ChartTimePeriodIndicatorView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var time_period_tv: TextView? = null
    private var indicator_iv: ImageView? = null

    init {
        inflate(context, R.layout.custom_view_chart_time_period_indicator, this)
        time_period_tv = findViewById(R.id.time_period)
        indicator_iv = findViewById(R.id.indicator)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ChartTimePeriodIndicatorView,
            0,
            0
        ).apply {
            val timePeriodText = getString(R.styleable.ChartTimePeriodIndicatorView_time_period_text) ?: ""
            val isSelected = getBoolean(R.styleable.ChartTimePeriodIndicatorView_is_selected, false)
            time_period_tv!!.text = timePeriodText

            selectTimePeriod(isSelected)
        }
    }

    fun selectTimePeriod(select: Boolean) {
        if(select) {
            indicator_iv!!.visibility = View.VISIBLE
        } else {
            indicator_iv!!.visibility = View.INVISIBLE
        }
    }
}

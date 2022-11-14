package com.intuisoft.plaid.androidwrappers

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.intuisoft.plaid.R

class TopBarView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var primary_text_tv: TextView? = null
    private var secondary_text_tv: TextView? = null
    private var left_action_iv: ImageView? = null
    private var right_action_iv: ImageView? = null


    private var primaryText: String = ""
    private var secondaryText: String = ""
    private var drawableRight = 0
    private var drawableLeft = 0
    var variant = LEFT_ALIGN

    companion object {
        // button text positions
        const val NO_BAR = 0
        const val LEFT_ALIGN = 1
        const val CENTER_ALIGN = 2
        const val CENTER_ALIGN_WHITE = 4
    }

    init {
        inflate(context, R.layout.custom_view_top_bar, this)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TopBarView,
            0,
            0
        ).apply {
            primaryText = getString(R.styleable.TopBarView_primary_text) ?: ""
            secondaryText = getString(R.styleable.TopBarView_secondary_text) ?: ""
            variant = getInt(R.styleable.TopBarView_top_bar_variant, NO_BAR)
            drawableRight = getResourceId(R.styleable.TopBarView_right_action, 0)
            drawableLeft = getResourceId(R.styleable.TopBarView_left_action, 0)

            setBarStyle(variant)
        }
    }

    fun setBarStyle(variant: Int) {
        when(variant) {
            LEFT_ALIGN -> {
                findViewById<ConstraintLayout>(R.id.top_bar_variant_left_align).isVisible = true
                findViewById<ConstraintLayout>(R.id.top_bar_variant_center_align).isVisible = false
                findViewById<ConstraintLayout>(R.id.top_bar_variant_center_align_white).isVisible = false
                primary_text_tv = findViewById(R.id.primary_text)
                secondary_text_tv = findViewById(R.id.secondary_text)
                right_action_iv = findViewById(R.id.action_right)
                left_action_iv = null
            }
            CENTER_ALIGN -> {
                findViewById<ConstraintLayout>(R.id.top_bar_variant_left_align).isVisible = false
                findViewById<ConstraintLayout>(R.id.top_bar_variant_center_align).isVisible = true
                findViewById<ConstraintLayout>(R.id.top_bar_variant_center_align_white).isVisible = false
                primary_text_tv = findViewById(R.id.center_variant_primary_text)
                secondary_text_tv = findViewById(R.id.center_variant_secondary_text)
                right_action_iv = findViewById(R.id.center_variant_action_right)
                left_action_iv = findViewById(R.id.center_variant_action_left)
            }
            CENTER_ALIGN_WHITE -> {
                findViewById<ConstraintLayout>(R.id.top_bar_variant_left_align).isVisible = false
                findViewById<ConstraintLayout>(R.id.top_bar_variant_center_align).isVisible = false
                findViewById<ConstraintLayout>(R.id.top_bar_variant_center_align_white).isVisible = true
                primary_text_tv = findViewById(R.id.white_center_variant_primary_text)
                secondary_text_tv = findViewById(R.id.white_center_variant_secondary_text)
                right_action_iv = findViewById(R.id.white_center_variant_action_right)
                left_action_iv = findViewById(R.id.white_center_variant_action_left)
            }
            else -> { // no bar
                findViewById<ConstraintLayout>(R.id.top_bar_variant_left_align).isVisible = false
                findViewById<ConstraintLayout>(R.id.top_bar_variant_center_align).isVisible = false
                findViewById<ConstraintLayout>(R.id.top_bar_variant_center_align_white).isVisible = false
            }
        }

        this.variant = variant
        setupView()
    }

    private fun setupView() {
        setPrimaryText(primaryText)
        setSecondaryText(secondaryText)
        setActionLeft(drawableLeft)
        setActionRight(drawableRight)

        secondary_text_tv?.doOnTextChanged { text, start, before, count ->
            secondary_text_tv?.isVisible = text?.isNotEmpty() ?: false
        }
    }

    fun setPrimaryText(value: String) {
        primaryText = value
        primary_text_tv?.text = value
        primary_text_tv?.isVisible = value.isNotEmpty()
    }

    fun setSecondaryText(value: String) {
        secondaryText = value
        secondary_text_tv?.text = value
        secondary_text_tv?.isVisible = value.isNotEmpty()
    }

    fun setActionLeft(icon: Int) {
        left_action_iv?.isVisible = icon != 0

        if(icon != 0) {
            left_action_iv?.setImageResource(icon)
        }
    }

    fun setActionRight(icon: Int) {
        right_action_iv?.isVisible = icon != 0

        if(icon != 0) {
            right_action_iv?.setImageResource(icon)
        }
    }

    fun setOnActionLeftClick(onClick: () -> Unit) {
        left_action_iv?.setOnClickListener {
            onClick()
        }
    }

    fun setOnActionRightClick(onClick: () -> Unit) {
        right_action_iv?.setOnClickListener {
            onClick()
        }
    }

    fun setSecondaryTextOnClick(onClick: () -> Unit) {
        secondary_text_tv?.setOnClickListener {
            onClick()
        }
    }
}

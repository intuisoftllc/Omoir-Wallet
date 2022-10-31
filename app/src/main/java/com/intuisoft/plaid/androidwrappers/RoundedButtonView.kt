package com.intuisoft.plaid.androidwrappers

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.intuisoft.plaid.R

class RoundedButtonView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var button: Button? = null
    private var text: String = ""
    private var btnGravity: Int = LEFT
    private var allCaps = false
    private var drawableRight = 0
    private var textSize = 16f
    private var padding = 0f
    private var tint = 0

    companion object {
        // button text positions
        const val CENTER = 0
        const val LEFT = 1
        const val RIGHT = 2
    }

    enum class ButtonStyle(val id: Int) {
        NO_STYLE(0),
        ROUNDED_STYLE(1),
        OUTLINED_STYLE(2),
        PILL_STYLE(4),
        TRANSPARENT_STYLE(8);
    }

    init {
        inflate(context, R.layout.custom_view_rounded_button, this)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RoundedButtonView,
            0,
            0
        ).apply {
            text = getString(R.styleable.RoundedButtonView_btn_text) ?: ""
            btnGravity = getInt(R.styleable.RoundedButtonView_text_position, LEFT)
            textSize = getInt(R.styleable.RoundedButtonView_text_size, 16).toFloat()
            padding = getDimension(R.styleable.RoundedButtonView_button_padding, 0f)
            allCaps = getBoolean(R.styleable.RoundedButtonView_text_all_caps, false)
            drawableRight = getResourceId(R.styleable.RoundedButtonView_right_icon, 0)
            tint = getColor(R.styleable.RoundedButtonView_button_tint, 0)

            setButtonStyle(getInt(R.styleable.RoundedButtonView_button_style, ButtonStyle.NO_STYLE.id))
        }
    }

    fun setButtonText(value: String) {
        button?.text = value
    }

    private fun setButtonTextPosition(value: Int) {
        when(value) {
            CENTER -> {
                button?.gravity = Gravity.CENTER
            }
            LEFT -> {
                button?.gravity = Gravity.START or Gravity.CENTER_VERTICAL
            }
            RIGHT -> {
                button?.gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }
            else -> {
                button?.gravity = Gravity.START
            }
        }
    }


    fun setButtonStyle(style: ButtonStyle) {
        setButtonStyle(style.id)
    }

    private fun setButtonStyle(value: Int) {
        button?.isVisible = false

        when(value) {
            ButtonStyle.NO_STYLE.id -> {
                button = findViewById(R.id.normal_button)
            }
            ButtonStyle.ROUNDED_STYLE.id -> {
                button = findViewById(R.id.rounded_style_button)
            }
            ButtonStyle.OUTLINED_STYLE.id -> {
                button = findViewById(R.id.rounded_outlined_style_button)
            }
            ButtonStyle.PILL_STYLE.id -> {
                button = findViewById(R.id.pill_style_button)
            }
            ButtonStyle.TRANSPARENT_STYLE.id -> {
                button = findViewById(R.id.transparent_style_button)
            }
            else -> {
                button = findViewById(R.id.normal_button)
            }
        }

        setupButton()
    }

    private fun setupButton() {
        setButtonText(text)
        setTextAllCaps(allCaps)
        setButtonTextPosition(btnGravity)
        setDrawableRight(drawableRight)
        setTextSize(textSize)
        setPadding(padding)
        setTint(tint)
        button?.isVisible = true
        button?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    button?.alpha = .8f
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    button?.alpha = 1f
                }
            }
            false
        }
    }

    private fun setTextSize(value: Float) {
        button?.textSize = value
    }

    private fun setTextAllCaps(value: Boolean) {
        button?.isAllCaps = value
    }

    private fun setPadding(value: Float) {
        if(value != 0f) {
            button?.setPadding(resources.dpToPixels(value).toInt(), 0, resources.dpToPixels(value).toInt(), 0)
        }
    }

    fun setTint(value: Int) {
        if(value != 0) {
            button?.tint(value)
        }
    }

    fun enableButton(enable: Boolean) {
        button?.isEnabled = enable
        if(enable)
            button?.alpha = 1f
        else
            button?.alpha = .5f
    }

    private fun setDrawableRight(drawable: Int) {
        button?.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
    }

    fun onClick(click: (Button) -> Unit) {
        findViewById<Button>(R.id.normal_button)?.setOnClickListener {
            click(button!!)
        }
        findViewById<Button>(R.id.rounded_style_button)?.setOnClickListener {
            click(button!!)
        }
        findViewById<Button>(R.id.rounded_outlined_style_button)?.setOnClickListener {
            click(button!!)
        }
        findViewById<Button>(R.id.pill_style_button)?.setOnClickListener {
            click(button!!)
        }
        findViewById<Button>(R.id.transparent_style_button)?.setOnClickListener {
            click(button!!)
        }
    }
}

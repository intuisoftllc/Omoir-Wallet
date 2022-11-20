package com.intuisoft.plaid.androidwrappers

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doBeforeTextChanged
import androidx.core.widget.doOnTextChanged
import com.intuisoft.plaid.R
import com.intuisoft.plaid.common.util.SimpleCoinNumberFormat
import com.intuisoft.plaid.common.util.extensions.deleteAt

class SwapPairItemView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var container: ConstraintLayout? = null
    private var swap_pair_ticker_container: LinearLayout? = null
    private var show_value_tv: TextView? = null
    private var enter_value_tv: EditText? = null
    private var ticker_tv: TextView? = null
    private var pair_title_tv: TextView? = null
    private var ticker_symbol_iv: ImageView? = null
    private var onTextChanged: ((String?) -> Boolean)? = null

    private var style = ENTER_VALUE_VARIANT_1
    private var ticker: String = ""
    private var title: String = ""
    private var symbol: Int = 0
    private var symbolUrl: String = ""
    private var value: Double = 0.0
    private var tickerClickable: Boolean = true

    companion object {
        // button text positions
        const val ENTER_VALUE_VARIANT_1 = 0
        const val ENTER_VALUE_VARIANT_2 = 1
        const val SHOW_VALUE_VARIANT_1 = 2
        const val SHOW_VALUE_VARIANT_2 = 4
    }

    init {
        inflate(context, R.layout.custom_view_swap_pair_item, this)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SwapPairItemView,
            0,
            0
        ).apply {
            val preview = getBoolean(R.styleable.SwapPairItemView_preview_item, false)
            val variant = getInt(R.styleable.SwapPairItemView_pair_preview, ENTER_VALUE_VARIANT_1)

            if(preview) {
                setStyle(variant)
            }
        }
    }

    fun setStyle(style: Int) {
        this.style = style
        container?.isVisible = false

        when(style) {
            ENTER_VALUE_VARIANT_1 -> {
                container = findViewById(R.id.swap_pair_variant_enter_value_1)
                enter_value_tv = findViewById(R.id.enter_pair_value_1)
                pair_title_tv = findViewById(R.id.send_get_title)
                ticker_tv = findViewById(R.id.ticker_1)
                ticker_symbol_iv = findViewById(R.id.ticker_symbol_1)
            }
            ENTER_VALUE_VARIANT_2 -> {
                container = findViewById(R.id.swap_pair_variant_enter_value_2)
                enter_value_tv = findViewById(R.id.enter_pair_value_2)
                pair_title_tv = findViewById(R.id.send_get_title2)
                swap_pair_ticker_container = findViewById(R.id.swap_pair_ticker_1)
                ticker_tv = findViewById(R.id.ticker_2)
                ticker_symbol_iv = findViewById(R.id.ticker_symbol_2)
            }
            SHOW_VALUE_VARIANT_1 -> {
                container = findViewById(R.id.swap_pair_variant_show_value_1)
                show_value_tv = findViewById(R.id.pair_value_1)
                pair_title_tv = findViewById(R.id.send_get_title3)
                swap_pair_ticker_container = findViewById(R.id.swap_pair_ticker_2)
                ticker_tv = findViewById(R.id.ticker_3)
                ticker_symbol_iv = findViewById(R.id.ticker_symbol_3)
            }
            SHOW_VALUE_VARIANT_2 -> {
                container = findViewById(R.id.swap_pair_variant_show_value_2)
                show_value_tv = findViewById(R.id.pair_value_2)
                pair_title_tv = findViewById(R.id.send_get_title4)
                ticker_tv = findViewById(R.id.ticker_4)
                ticker_symbol_iv = findViewById(R.id.ticker_symbol_4)
            }
        }

        container?.isVisible = true
        setTicker(ticker)
        if(symbol != 0) setTickerSymbol(symbol)
        else setTickerSymbol(symbolUrl)
        setValue(value)
        setPairTitle(title)
        setTickerClickable(tickerClickable)
        onTextChanged?.let { setOnTextChangedListener(it) }
    }

    fun setTicker(name: String) {
        ticker = name
        ticker_tv?.text = ticker
    }

    fun setPairTitle(title: String) {
        this.title = title
        pair_title_tv?.text = title
    }

    fun setTickerSymbol(drawable: Int) {
        symbol = drawable
        symbolUrl = ""

        if(drawable != 0)
            ticker_symbol_iv?.setImageResource(symbol)
        else ticker_symbol_iv?.setImageDrawable(null)
    }

    fun setTickerSymbol(image: String) {
        symbolUrl = image
        symbol = 0

        if(image != "") {
            ticker_symbol_iv?.loadUrl(symbolUrl)
        }
        else ticker_symbol_iv?.setImageDrawable(null)
    }

    fun setValue(value: Double) {
        this.value = value
        var valueStr = SimpleCoinNumberFormat.formatCurrency(value) ?: ""

        if(valueStr.endsWith(".00"))
            valueStr = valueStr.dropLast(3)

        show_value_tv?.text = "~$valueStr"

        if(value != 0.0)
            enter_value_tv?.setText(valueStr)
        else enter_value_tv?.setText("")
    }

    fun setTickerClickable(clickable: Boolean) {
        tickerClickable = clickable
        swap_pair_ticker_container?.isClickable = clickable
    }

    fun onTickerClicked(onClick: () -> Unit) {
        swap_pair_ticker_container?.setOnClickListener {
            onClick()
        }
    }

    fun setOnTextChangedListener(onChanged: (String?) -> Boolean) {
        onTextChanged = onChanged

        enter_value_tv?.doOnTextChanged { text, start, before, count ->
            if(!onChanged(text?.toString())) {

                if(text?.isNotEmpty() == true) {
                    enter_value_tv?.setText(text.toString().deleteAt(start))
                    enter_value_tv?.setSelection(enter_value_tv?.length() ?: 0)
                }

                val shake: Animation = AnimationUtils.loadAnimation(context, R.anim.shake)
                enter_value_tv?.startAnimation(shake)
            }
        }
    }
}

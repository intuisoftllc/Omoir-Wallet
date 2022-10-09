package com.intuisoft.plaid.androidwrappers

import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.intuisoft.plaid.R

class SeedWordView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var word_tv: TextView? = null
    private var index_tv: TextView? = null

    init {
        inflate(context, R.layout.seed_word_view, this)
        word_tv = findViewById(R.id.seedWord)
        index_tv = findViewById(R.id.wordIndex)
    }

    fun setWord(value: String) {
        word_tv?.text = value
    }

    fun setIndex(value: Int) {
        index_tv?.text = value.toString()
    }
}

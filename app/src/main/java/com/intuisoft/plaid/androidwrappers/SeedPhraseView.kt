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

class SeedPhraseView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var container1: LinearLayout? = null
    private var container2: LinearLayout? = null
    private var container3: LinearLayout? = null
    private var container4: LinearLayout? = null
    private var container5: LinearLayout? = null
    private var container6: LinearLayout? = null
    private var seedWords: MutableList<SeedWordView> = mutableListOf()
    private var currentWord = 0

    init {
        inflate(context, R.layout.custom_view_seed_phrase, this)
        container1 = findViewById(R.id.seedWordContainer1)
        container2 = findViewById(R.id.seedWordContainer2)
        container3 = findViewById(R.id.seedWordContainer3)
        container4 = findViewById(R.id.seedWordContainer4)
        container5 = findViewById(R.id.seedWordContainer5)
        container6 = findViewById(R.id.seedWordContainer6)
        seedWords.add(findViewById(R.id.word_1))
        seedWords.add(findViewById(R.id.word_2))
        seedWords.add(findViewById(R.id.word_3))
        seedWords.add(findViewById(R.id.word_4))
        seedWords.add(findViewById(R.id.word_5))
        seedWords.add(findViewById(R.id.word_6))
        seedWords.add(findViewById(R.id.word_7))
        seedWords.add(findViewById(R.id.word_8))
        seedWords.add(findViewById(R.id.word_9))
        seedWords.add(findViewById(R.id.word_10))
        seedWords.add(findViewById(R.id.word_11))
        seedWords.add(findViewById(R.id.word_12))
        seedWords.add(findViewById(R.id.word_13))
        seedWords.add(findViewById(R.id.word_14))
        seedWords.add(findViewById(R.id.word_15))
        seedWords.add(findViewById(R.id.word_16))
        seedWords.add(findViewById(R.id.word_17))
        seedWords.add(findViewById(R.id.word_18))
        seedWords.add(findViewById(R.id.word_19))
        seedWords.add(findViewById(R.id.word_20))
        seedWords.add(findViewById(R.id.word_21))
        seedWords.add(findViewById(R.id.word_22))
        seedWords.add(findViewById(R.id.word_23))
        seedWords.add(findViewById(R.id.word_24))
    }

    fun resetView() {
        container1?.isVisible = false
        container2?.isVisible = false
        container3?.isVisible = false
        container4?.isVisible = false
        container5?.isVisible = false
        container6?.isVisible = false

        seedWords.forEach {
            it.isVisible = false
        }
    }

    fun nextWord(value: String) {
        showWordContainer()

        if(currentWord < 24) {
            val word = seedWords[currentWord]

            word.isVisible = true
            word.setIndex(currentWord + 1)
            word.setWord(value)

            currentWord++
        }
    }

    fun removeLastWord() {
        if(currentWord > 0) {
            seedWords[currentWord - 1].isVisible = false


            if(currentWord == 0) {
                container1?.isVisible = false
            } else if(currentWord == 4) {
                container2?.isVisible = false
            } else if(currentWord ==8) {
                container3?.isVisible = false
            } else if(currentWord == 12) {
                container4?.isVisible = false
            } else if(currentWord == 16) {
                container5?.isVisible = false
            } else if(currentWord == 20) {
                container6?.isVisible = false
            }

            currentWord--
        }
    }

    private fun showWordContainer() {
        when {
            (0..3).contains(currentWord) -> {
                container1?.isVisible = true
            }
            (4..7).contains(currentWord) -> {
                container2?.isVisible = true
            }
            (8..11).contains(currentWord) -> {
                container3?.isVisible = true
            }
            (12..15).contains(currentWord) -> {
                container4?.isVisible = true
            }
            (16..19).contains(currentWord) -> {
                container5?.isVisible = true
            }
            (20..23).contains(currentWord) -> {
                container6?.isVisible = true
            }
        }
    }

    interface RecoveryPhraseUpdatedListener {
        fun onUpdate()
    }
}

package com.intuisoft.plaid.androidwrappers

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable


/**
 * CircleView
 * Created by hanks on 2017/4/17.
 */
class CircleView : View {
    private var mPaint: Paint? = null
    private var color = Color.BLACK

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, @Nullable attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, @Nullable attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    }

    fun setColor(color: Int) {
        this.color = color
        invalidate()
    }

    fun getColor(): Int {
        return color
    }

    override fun onDraw(canvas: Canvas) {
        val width = (width - paddingLeft - paddingRight) * 0.5
        val height = (height - paddingTop - paddingBottom) * 0.5
        val cx = (paddingLeft + width).toInt()
        val cy = (paddingTop + height).toInt()
        val radius = Math.min(width, height).toInt()
        mPaint!!.color = color
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius.toFloat(), mPaint!!)
    }
}

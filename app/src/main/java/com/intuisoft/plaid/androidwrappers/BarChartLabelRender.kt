package com.intuisoft.plaid.androidwrappers

import android.graphics.Canvas
import android.graphics.Paint
import android.text.TextPaint
import com.db.williamchart.Labels
import com.db.williamchart.data.Label


class BarChartLabelRender : Labels {
    private var data: List<Pair<String, Float>> = listOf()

    private var emptyDataLabelColor: Int = -0x1000000

    private var hasDataLabelColor: Int = -0x1000000

    fun prepare(emptyDataColor: Int, hasDataColor: Int, data: List<Pair<String, Float>>) {
        this.data = data
        this.emptyDataLabelColor = emptyDataColor
        this.hasDataLabelColor = hasDataColor
    }

    override fun draw(canvas: Canvas, paint: Paint, xLabels: List<Label>) {
        xLabels.forEachIndexed { index, label ->
            val hasData = data[index].second != 0.0f
            if(!hasData) {
                paint.color = emptyDataLabelColor
            } else {
                paint.color = hasDataLabelColor
            }

            drawString(
                canvas,
                label.label,
                label.screenPositionX,
                label.screenPositionY,
                TextPaint(paint)
            )
        }
    }

    private fun drawString(canvas: Canvas, text: String, x: Float, y: Float, paint: TextPaint) {
        var y = y
        if (text.contains("\n")) {
            val texts = text.split("\n").toTypedArray()
            for (txt in texts) {
                canvas.drawText(txt, x, y-15, paint)
                y += paint.textSize.toInt()
            }
        } else {
            canvas.drawText(text, x, y, paint)
        }
    }
}
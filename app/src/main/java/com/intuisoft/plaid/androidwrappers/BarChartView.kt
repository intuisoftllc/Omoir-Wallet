package com.intuisoft.plaid.androidwrappers

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.core.view.doOnPreDraw
import com.db.williamchart.ChartContract
import com.db.williamchart.ExperimentalFeature
import com.db.williamchart.animation.DefaultHorizontalAnimation
import com.db.williamchart.animation.NoAnimation
import com.db.williamchart.data.*
import com.db.williamchart.data.configuration.BarChartConfiguration
import com.db.williamchart.data.configuration.ChartConfiguration
import com.db.williamchart.extensions.drawChartBar
import com.db.williamchart.extensions.obtainStyledAttributes
import com.db.williamchart.renderer.HorizontalBarChartRenderer
import com.db.williamchart.view.AxisChartView
import com.intuisoft.plaid.R

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AxisChartView(context, attrs, defStyleAttr), ChartContract.BarView {

    @Suppress("MemberVisibilityCanBePrivate")
    var spacing = defaultSpacing

    @ColorInt
    @Suppress("MemberVisibilityCanBePrivate")
    var barsColor: Int = defaultBarsColor

    @ColorInt
    @Suppress("MemberVisibilityCanBePrivate")
    var negativeBarsColor: Int = defaultNegativeBarsColor

    @Suppress("MemberVisibilityCanBePrivate")
    var barsColorsList: List<Int>? = null

    @ExperimentalFeature
    @Size(2)
    @Suppress("MemberVisibilityCanBePrivate")
    var barsGradientColors: IntArray? = null

    @Suppress("MemberVisibilityCanBePrivate")
    var barRadius: Float = defaultBarsRadius

    @Suppress("MemberVisibilityCanBePrivate")
    var barsBackgroundColor: Int = -1

    @Suppress("MemberVisibilityCanBePrivate")
    var emptyDataLabelColor: Int = labelsColor

    @Suppress("MemberVisibilityCanBePrivate")
    var barsSelectedColor: Int = defaultBarsColor

    @Suppress("MemberVisibilityCanBePrivate")
    var negativeBarSelectedColor: Int = defaultNegativeBarsColor

    @Suppress("MemberVisibilityCanBePrivate")
    var isHorizontal: Boolean = false

    @Suppress("MemberVisibilityCanBePrivate")
    var listener: BarSelectedListener? = null

    @Suppress("MemberVisibilityCanBePrivate")
    var sizeConfig: BarChartSizeConfiguration? = null
        set(value) {
            field = value
            field?.let {
                spacing = resources.dpToPixels(it.spacing)
            }
        }

    @Suppress("MemberVisibilityCanBePrivate")
    var data: List<Pair<String, Float>> = listOf()
        set(value) {
            field = value

            lastSelectedBar = -1
            barsColorsList = null
            if(sizeConfig == null)
                throw java.lang.IllegalStateException("Bar size not set!")

            animate(field)
        }

    private val onBarSelected: (index: Int, x: Float, y: Float) -> Unit= { index, _, _ ->
        if(data[index].second != 0.0f) {
            if (index == lastSelectedBar) {
                lastSelectedBar = -1
                listener?.onDeSelected(index)
            } else {
                lastSelectedBar = index
                listener?.onSelected(index)
            }

            barsColorsList = null
            showAfterClicked(data)
        }
    }

    private var lastSelectedBar: Int = -1

    private val labelRender = BarChartLabelRender()

    override val chartConfiguration: ChartConfiguration
        get() =
            BarChartConfiguration(
                width = measuredWidth,
                height = measuredHeight,
                paddings = Paddings(
                    paddingLeft.toFloat(),
                    paddingTop.toFloat(),
                    paddingRight.toFloat(),
                    paddingBottom.toFloat()
                ),
                axis = axis,
                labelsSize = labelsSize,
                scale = scale,
                barsBackgroundColor = barsBackgroundColor,
                barsSpacing = spacing,
                labelsFormatter = labelsFormatter
            )

    init {
        handleAttributes(obtainStyledAttributes(attrs, R.styleable.BarChartAttrs))

        if(isHorizontal) {
            animation = DefaultHorizontalAnimation()
            renderer = HorizontalBarChartRenderer(this, painter, NoAnimation())
        } else {
            renderer = BarChartRender(this, painter, NoAnimation())
        }
        onDataPointClickListener = onBarSelected
        handleEditMode()
    }

    override fun drawBars(frames: List<Frame>) {

        if (barsGradientColors == null) {

            if (barsColorsList == null)
                barsColorsList = List(frames.size) {
                    if(it == lastSelectedBar) {
                        if(data[it].second < 0.0f)
                            negativeBarSelectedColor
                        else barsSelectedColor
                    }
                    else {
                        if (data[it].second < 0.0f)
                            negativeBarsColor
                        else barsColor
                    }
                }.toList()

            if (barsColorsList!!.size != frames.size)
                throw IllegalArgumentException("Colors provided do not match the number of datapoints.")
        }

        frames.forEachIndexed { index, frame ->
            if (barsGradientColors != null) {
                painter.prepare(
                    shader = frame.toLinearGradient(barsGradientColors!!),
                    style = Paint.Style.FILL
                )
            } else {
                painter.prepare(color = barsColorsList!![index], style = Paint.Style.FILL)
            }

            canvas.drawChartBar(
                frame.toRectF(),
                barRadius,
                painter.paint
            )
        }
    }

    override fun drawBarsBackground(frames: List<Frame>) {
        painter.prepare(color = barsBackgroundColor, style = Paint.Style.FILL)
        frames.forEach {
            canvas.drawChartBar(
                it.toRectF(),
                barRadius,
                painter.paint
            )
        }
    }

    override fun drawLabels(xLabels: List<Label>) {
        painter.prepare(textSize = labelsSize, color = labelsColor, font = labelsFont)
        labelRender.prepare(emptyDataLabelColor, labelsColor, data)
        labelRender.draw(canvas, painter.paint, xLabels)
    }

    override fun drawGrid(
        innerFrame: Frame,
        xLabelsPositions: List<Float>,
        yLabelsPositions: List<Float>
    ) {
        grid.draw(canvas, innerFrame, xLabelsPositions, yLabelsPositions)
    }

    private fun showAfterClicked(entries: List<Pair<String, Float>>) {
        doOnPreDraw { renderer.preDraw(chartConfiguration) }
        renderer.anim(entries, NoAnimation())
    }

    override fun drawDebugFrame(frames: List<Frame>) {
        painter.prepare(color = -0x1000000, style = Paint.Style.STROKE)
        frames.forEach { canvas.drawRect(it.toRect(), painter.paint) }
    }

    private fun handleAttributes(typedArray: TypedArray) {
        typedArray.apply {
            spacing = getDimension(R.styleable.BarChartAttrs_chart_spacing, spacing)
            barsColor = getColor(R.styleable.BarChartAttrs_chart_barsColor, barsColor)
            barsSelectedColor = getColor(R.styleable.BarChartAttrs_chart_barSelectedColor, barsSelectedColor)
            negativeBarsColor = getColor(R.styleable.BarChartAttrs_chart_negativeBarsColor, negativeBarsColor)
            negativeBarSelectedColor = getColor(R.styleable.BarChartAttrs_chart_negativeBarSelectedColor, negativeBarSelectedColor)
            isHorizontal = getBoolean(R.styleable.BarChartAttrs_chart_isHorizontal, isHorizontal)
            emptyDataLabelColor = getColor(R.styleable.BarChartAttrs_chart_emptyDataLabelColor, emptyDataLabelColor)
            barRadius = getDimension(R.styleable.BarChartAttrs_chart_barsRadius, barRadius)
            barsBackgroundColor =
                getColor(R.styleable.BarChartAttrs_chart_barsBackgroundColor, barsBackgroundColor)
            val resourceId = getResourceId(R.styleable.BarChartAttrs_chart_barsColorsList, -1)
            if (resourceId != -1)
                barsColorsList = resources.getIntArray(resourceId).toList()
            recycle()
        }
    }

    companion object {
        private const val defaultSpacing = 10f
        private const val defaultBarsColor = Color.BLACK
        private const val defaultNegativeBarsColor = Color.RED
        private const val defaultBarsRadius = 0F
    }
}

package com.intuisoft.plaid.androidwrappers

import com.db.williamchart.data.DataPoint
import com.db.williamchart.data.Frame
import com.db.williamchart.data.configuration.ChartConfiguration


fun List<Pair<String, Float>>.toDataPoints(): List<DataPoint> =
    map {
        DataPoint(
            label = it.first,
            value = it.second,
            screenPositionX = 0f,
            screenPositionY = 0f
        )
    }


fun ChartConfiguration.toOuterFrame(): Frame {
    return Frame(
        left = paddings.left,
        top = paddings.top,
        right = width - paddings.right,
        bottom = height - paddings.bottom
    )
}

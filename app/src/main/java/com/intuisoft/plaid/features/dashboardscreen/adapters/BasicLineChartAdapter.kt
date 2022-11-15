package com.intuisoft.plaid.features.dashboardscreen.adapters

import com.robinhood.spark.SparkAdapter

class BasicLineChartAdapter : SparkAdapter() {
    private var yData: FloatArray = floatArrayOf()

    override fun getCount(): Int {
        return yData.size
    }

    override fun getItem(index: Int): Any {
        return yData[index]
    }

    override fun getY(index: Int): Float {
        return yData[index]
    }

    fun setItems(data: FloatArray) {
        yData = data
        notifyDataSetChanged()
    }
}
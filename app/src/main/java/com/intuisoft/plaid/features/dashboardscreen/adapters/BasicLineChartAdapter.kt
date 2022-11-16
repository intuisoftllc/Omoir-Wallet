package com.intuisoft.plaid.features.dashboardscreen.adapters

import com.intuisoft.plaid.common.model.ChartDataModel
import com.robinhood.spark.SparkAdapter

class BasicLineChartAdapter : SparkAdapter() {
    private var data: List<ChartDataModel> = listOf()

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(index: Int): Any {
        return data[index]
    }

    override fun getY(index: Int): Float {
        return data[index].value
    }

    fun setItems(data: List<ChartDataModel>) {
        this.data = data
        notifyDataSetChanged()
    }
}
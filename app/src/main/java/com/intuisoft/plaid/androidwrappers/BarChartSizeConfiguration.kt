package com.intuisoft.plaid.androidwrappers

enum class BarChartSizeConfiguration(
    val barCount: Int,
    val spacing: Float
) {
    CHART_SIZE_SMALL(6, 40f),
    CHART_SIZE_MEDIUM(7, 32f),
    CHART_SIZE_MEDIUM_LARGE(8, 26f),
    CHART_SIZE_LARGE(12, 15f)
}
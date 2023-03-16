package com.intuisoft.plaid.model

import com.intuisoft.plaid.R

enum class ValueFilter(val displayIcon: Int) {
    LESS_THAN(R.drawable.ic_less_than),
    GREATER_THAN(R.drawable.ic_greater_than),
    LESS_THAN_EQ(R.drawable.ic_less_or_equal),
    GREATER_THAN_EQ(R.drawable.ic_greater_than_or_equal),
    EQUAL_TO(R.drawable.ic_equal_to),
    NOT_EQUAL_TO(R.drawable.ic_not_equal)
}
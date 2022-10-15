package com.intuisoft.plaid.util

object Plural {

    fun of(type: String, count: Long, postfix: String = "s") : String {
        if(count == 1L)
            return "$count $type"
        else return "$count ${type}$postfix"
    }
}
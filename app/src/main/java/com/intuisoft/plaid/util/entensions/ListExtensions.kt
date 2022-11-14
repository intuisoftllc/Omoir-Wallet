package com.intuisoft.plaid.util.entensions

import com.intuisoft.plaid.util.Group


fun <T> List<T>.splitIntoGroupOf(size: Int) : List<Group<T>> {
    if(size <= 0) throw java.lang.IllegalStateException("Size <= 0")
    val list = mutableListOf<Group<T>>()
    val limiter = size - 1

    if(size == 1) {
        return this.map { Group(mutableListOf(it)) }
    } else if(this.size < size) {
        list.add(Group(this.toMutableList()))
        return list
    } else {
        var index  = 0

        while(index < this.size) {
            if((index + limiter) < this.size) {
                list.add(Group(this.slice(index..(index + limiter)).toMutableList()))
            } else {
                list.add(Group(this.takeLast(this.size - index).toMutableList()))
            }

            index += limiter+1
        }

        return list
    }
}
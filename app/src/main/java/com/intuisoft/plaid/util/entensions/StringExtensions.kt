package com.intuisoft.plaid.util.entensions

import java.math.BigInteger
import java.security.MessageDigest

fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

fun String.shiftRight(char: Char, amount: Int) : String {
    var index = indexOf(char)
    var str = this.replace(char.toString(), "")
    str = str.replaceRange(index + amount, index + amount, char.toString())
    return str
}

fun String.charsAfter(ch: Char) : Int {
    var charsFound = 0

    this.forEachIndexed { index, c ->
        if(c == ch) {
            return this.length - (index + 1)
        }
    }

    return 0
}

fun String.addChars(ch: Char, count: Int) : String {

    var i = 0
    var newStr = this
    while(i < count) {
        newStr += ch
        i++
    }

    return newStr
}
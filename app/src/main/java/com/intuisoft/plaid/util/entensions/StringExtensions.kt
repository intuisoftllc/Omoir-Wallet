package com.intuisoft.plaid.util.entensions

import java.math.BigInteger
import java.security.MessageDigest

fun String.sha256(): String {
    val md = MessageDigest.getInstance("SHA-256")
    return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}

fun String.shiftRight(char: Char, amount: Int) : String {
    var index = indexOf(char)
    var str = str.replace(char, "")
    str = str.replaceRange(index + amount, index + amount, char)
    return str
}
package com.intuisoft.plaid.common.util.extensions

import android.content.Context
import android.text.Editable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.intuisoft.plaid.common.util.Group
import com.intuisoft.plaid.common.util.errors.ClosedWalletErr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.*
import java.security.SecureRandom
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.math.pow
import kotlin.math.roundToInt

@FlowPreview
public fun <T> (suspend () -> T).asFlow(): Flow<T> = flow {
    emit(invoke())
}

fun safeWalletScope(block: () -> Unit) {
    try {
        block()
    } catch(_: ClosedWalletErr) {}
}

fun String.numOfCaseLetters(uppercase: Boolean): Int {
    var letterCount = 0
    this.forEach { c ->
        if(uppercase) {
            if (c.isLetter() && c.isUpperCase()) {
                letterCount++
            }
        } else {
            if (c.isLetter() && c.isLowerCase()) {
                letterCount++
            }
        }

    }

    return letterCount
}

fun Double.ignoreNan(): Double {
    if(this.isNaN())
        return 0.0
    else return this
}

fun String.numberCount(): Int {
    var numCount = 0
    this.forEach { c ->
        if (c.isDigit()) {
            numCount++
        }
    }

    return numCount
}

fun String.deleteAt(n: Int): String {
    return StringBuilder(this).deleteAt(n).toString()
}

fun Editable.toStringOrNull() : String? {
    val str = toString()

    if(str.isEmpty()) return null
    else return str
}

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

fun <T> ArrayList<T>.addIf(value: T, predicate: (T, List<T>) -> Boolean) {
    if(predicate(value, this))
        add(value)
}

fun <T> ArrayList<T>.addAll(items: List<T>) {
    items.forEach {
        add(it)
    }
}

fun <T> ArrayList<T>.addAllIf(items: List<T>, predicate: (T, List<T>) -> Boolean) {
    items.forEach {
        if(predicate(it, this))
            add(it)
    }
}

fun <T> List<T>.toArrayList() : ArrayList<T> {
    val list = arrayListOf<T>()
    this.forEach {
        list.add(it)
    }

    return list
}

fun  <T> MutableList<T>.prepend(item: T) : MutableList<T> {
    this.add(0, item)
    return this
}

fun <T> MutableList<T>.remove(predicate: (T) -> Boolean) {
    val newList: MutableList<T> = ArrayList()
    filter { predicate(it) }.forEach { newList.add(it) }
    removeAll(newList)
}

fun String.toHtmlLink(link: String) : String =
    String.format("<a href=\"%s\">$this</a> ", link)

public suspend fun <T> LiveData<T>.await(): T {
    return withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine { continuation ->
            val observer = object : Observer<T> {
                override fun onChanged(value: T) {
                    removeObserver(this)
                    continuation.resume(value) {

                    }
                }
            }

            observeForever(observer)

            continuation.invokeOnCancellation {
                removeObserver(observer)
            }
        }
    }
}

fun Long.humanReadableByteCountSI(): String? {
    var bytes = this
    if (-1000 < bytes && bytes < 1000) {
        return "$bytes B"
    }
    val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
    while (bytes <= -999950 || bytes >= 999950) {
        bytes /= 1000
        ci.next()
    }
    return java.lang.String.format("%.1f %cb", bytes / 1000.0, ci.current())
}

fun String.containsNumbers(): Boolean {
    return this.find { Character.isDigit(it) } != null
}

fun File.writeToFile(data: String, context: Context): Boolean {
    return try {
        val outputStreamWriter =
            OutputStreamWriter(context.openFileOutput(name, Context.MODE_PRIVATE))
        outputStreamWriter.write(data)
        outputStreamWriter.close()
        true
    } catch (e: IOException) {
        Log.e("FileWriter", "File write failed: " + e.toString())
        false
    }
}

fun File.readFromFile(context: Context): String? {
    var ret = ""
    try {
        val inputStream: InputStream? = context.openFileInput(name)
        if (inputStream != null) {
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var receiveString: String? = ""
            val stringBuilder = java.lang.StringBuilder()
            while (bufferedReader.readLine().also { receiveString = it } != null) {
                stringBuilder.append("\n").append(receiveString)
            }
            inputStream.close()
            ret = stringBuilder.toString()
        }
    } catch (e: FileNotFoundException) {
        Log.e("FileWriter", "File not found: " + e.toString())
    } catch (e: IOException) {
        Log.e("FileWriter", "Can not read file: $e")
    }
    return ret
}

fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor) / factor
}

fun List<Long>.median(): Double {
    val array = this.sorted()

    if (array.size % 2 == 0) {
        return ((array[array.size / 2] + array[array.size / 2 - 1]) / 2).toDouble()
    } else {
        return (array[array.size / 2]).toDouble()
    }
}

fun SecureRandom.nextInt(low: Int, high: Int): Int {
    var n: Int
    while(true) {
        n = nextInt(high + 1)
        if(n in low..high) break
    }
    return n
}

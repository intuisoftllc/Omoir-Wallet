package com.intuisoft.plaid.util

import com.intuisoft.plaid.common.util.Constants
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit

object SimpleTimeFormat {

    fun timeToString(time: Long): String? {
        var convTime: String? = null
        val prefix = ""
        val suffix = "Ago"
        val pasTime: Date = Date(time * 1000)
        val nowTime = Date()
        val dateDiff: Long = nowTime.getTime() - pasTime.getTime()
        val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
        val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
        val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
        val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
        val week = day / 7
        val month = week / 4
        val year = month / 12
        if (second < 60) {
            convTime = Plural.of("Second", second) + " $suffix"
        } else if (minute < 60) {
            convTime = Plural.of("Minute", minute) + " $suffix"
        } else if (hour < 24) {
            convTime = Plural.of("Hour", hour) + " $suffix"
        } else if (day < 30) {
            convTime = Plural.of("Day", day) + " $suffix"
        } else if (month < 12) {
            convTime = Plural.of("Month", month) + " $suffix"
        } else {
            convTime = Plural.of("Year", year) + " $suffix"
        }

        return convTime
    }

    fun isToday(mills: Long) : Boolean {
        val dateDays = (mills / Constants.Time.MILLS_PER_SEC) / Constants.Time.SECONDS_PER_DAY
        val currentDay = (System.currentTimeMillis() / Constants.Time.MILLS_PER_SEC) / Constants.Time.SECONDS_PER_DAY

        return dateDays == currentDay
    }

    fun isYesterday(mills: Long) : Boolean {
        val dateDays = (mills / Constants.Time.MILLS_PER_SEC) / Constants.Time.SECONDS_PER_DAY
        val currentDay = (System.currentTimeMillis() / Constants.Time.MILLS_PER_SEC) / Constants.Time.SECONDS_PER_DAY

        return (dateDays - 1) == (currentDay - 1)
    }

    fun isSameYear(mills: Long): Boolean {
        val z: ZoneId = ZoneId.of(ZoneId.systemDefault().id)
        val referenceTime: ZonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(mills), z)
        val nowTime: ZonedDateTime = ZonedDateTime.now()

        return referenceTime.year == nowTime.year
    }

    fun getDateByLocale(timeInMills: Long, locale: String?): String? {
        return SimpleDateFormat("MMM dd, yyyy hh:mm aa", Locale(locale)).format(Date(timeInMills))
    }

    fun getShortHistoryDateByLocale(timeInMills: Long, locale: String?): String? {
        return SimpleDateFormat("MMM dd", Locale(locale)).format(Date(timeInMills))
    }

    fun getLongHistoryDateByLocale(timeInMills: Long, locale: String?): String? {
        return SimpleDateFormat("MMM dd, yyyy", Locale(locale)).format(Date(timeInMills))
    }
}
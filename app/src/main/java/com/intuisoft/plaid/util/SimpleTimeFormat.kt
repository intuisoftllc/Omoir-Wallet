package com.intuisoft.plaid.util

import com.intuisoft.plaid.common.model.ReportHistoryTimeFilter
import com.intuisoft.plaid.common.util.Constants
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToLong

object SimpleTimeFormat {

    fun timeToString(time: Long, suffix: String = "Ago"): String {
        var convTime: String
        val pasTime: Date = Date(time)
        val nowTime = Date()
        val dateDiff: Long

        if(pasTime < nowTime)
            dateDiff = nowTime.getTime() - pasTime.getTime()
        else
            dateDiff = pasTime.getTime() - nowTime.getTime()

        return internalTimeToString(dateDiff, suffix)
    }

    private fun internalTimeToString(dateDiff: Long, suffix: String): String {
        var convTime: String
        val second: Long = TimeUnit.MILLISECONDS.toSeconds(dateDiff)
        val minute: Long = TimeUnit.MILLISECONDS.toMinutes(dateDiff)
        val hour: Long = TimeUnit.MILLISECONDS.toHours(dateDiff)
        val day: Long = TimeUnit.MILLISECONDS.toDays(dateDiff)
        val week = (day.toDouble() / 7).roundToLong()
        val month = (week.toDouble() / 4).roundToLong()
        val year = (month.toDouble() / 12).roundToLong()
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

        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(mills), ZoneId.systemDefault()).dayOfMonth == ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).dayOfMonth
    }

    fun isYesterday(mills: Long) : Boolean {
        val dateDays = (mills / Constants.Time.MILLS_PER_SEC) / Constants.Time.SECONDS_PER_DAY
        val currentDay = (System.currentTimeMillis() / Constants.Time.MILLS_PER_SEC) / Constants.Time.SECONDS_PER_DAY

        return (dateDays - 1) == (currentDay - 1)
    }

    fun isSameYear(mills: Long): Boolean {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(mills), ZoneId.systemDefault()).year ==
                startOfDay(ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())).plusSeconds(1).year
    }

    fun startOfDay(date: ZonedDateTime) = date.toLocalDate().atStartOfDay(date.zone)
    fun endOfDay(date: ZonedDateTime) = startOfDay(date).plusDays(1).minusSeconds(1)

    fun getTimePeriodsFor(filter: ReportHistoryTimeFilter, birthdate: ZonedDateTime) : List<Pair<String, Pair<Instant, Instant>>> {
        var nowTime: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())
        val periods = mutableListOf<Pair<String, Pair<Instant, Instant>>>()

        when {
            filter == ReportHistoryTimeFilter.LAST_WEEK || (filter == ReportHistoryTimeFilter.ALL_TIME && startOfDay(birthdate).toEpochSecond() >= startOfDay(nowTime).minusWeeks(1).toEpochSecond()) -> {
                var day = startOfDay(nowTime)
                var eod = endOfDay(nowTime)

                periods.add(
                    day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US) to (day.toInstant() to eod.toInstant())
                )

                var daysLeft = 6
                while(daysLeft > 0) {
                    nowTime = nowTime.minusDays(1)
                    day = startOfDay(nowTime)
                    eod = endOfDay(nowTime)

                    periods.add(
                        day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.US) to (day.toInstant() to eod.toInstant())
                    )
                    daysLeft--
                }

                return periods.reversed()
            }

            filter == ReportHistoryTimeFilter.LAST_MONTH || (filter == ReportHistoryTimeFilter.ALL_TIME && startOfDay(birthdate).toEpochSecond() >= startOfDay(nowTime).minusMonths(1).toEpochSecond()) -> {
                var startTime = startOfDay(nowTime).minusMonths(1)
                var endTime: ZonedDateTime
                var daysBetween = (startOfDay(nowTime).toEpochSecond() / Constants.Time.SECONDS_PER_DAY) - (startTime.toEpochSecond() / Constants.Time.SECONDS_PER_DAY)

                while(daysBetween > 0) {

                    if(daysBetween >=4) {
                        endTime = startTime.plusDays(4)

                        periods.add(
                            startTime.format(DateTimeFormatter.ofPattern("MMM dd")) to (startTime.toInstant() to endTime.toInstant().minusSeconds(1))
                        )

                        startTime = endTime
                        daysBetween -= 4
                    } else {
                        endTime = startTime.plusDays(daysBetween + 1)

                        periods.add(
                            startTime.format(DateTimeFormatter.ofPattern("MMM dd")) to (startTime.toInstant() to endTime.toInstant().minusSeconds(1))
                        )

                        break
                    }
                }

                return periods
            }

            filter == ReportHistoryTimeFilter.LAST_6MONTHS || (filter == ReportHistoryTimeFilter.ALL_TIME && startOfDay(birthdate).toEpochSecond() >= startOfDay(nowTime).minusMonths(6).toEpochSecond()) -> {
                var startTime = startOfDay(nowTime).minusDays(nowTime.dayOfMonth.toLong() - 1).minusMonths(5)
                var endTime: ZonedDateTime
                var monthsLeft = 6

                while(monthsLeft > 0) {
                    if(monthsLeft == 1) {
                        endTime = nowTime
                    } else {
                        endTime = startTime.plusMonths(1)
                    }

                    periods.add(
                        "${startTime.format(DateTimeFormatter.ofPattern("MMM"))}\n'${endTime.minusSeconds(1).format(DateTimeFormatter.ofPattern("YY"))}" to (startTime.toInstant() to endTime.toInstant().minusSeconds(1))
                    )

                    startTime = endTime
                    monthsLeft--
                }

                return periods
            }

            filter == ReportHistoryTimeFilter.LAST_YEAR || (filter == ReportHistoryTimeFilter.ALL_TIME && startOfDay(birthdate).toEpochSecond() >= startOfDay(nowTime).minusYears(1).toEpochSecond()) -> {
                var startTime = startOfDay(nowTime).minusDays(nowTime.dayOfMonth.toLong() - 1).minusMonths(11)
                var endTime: ZonedDateTime
                var monthsLeft = 12

                while(monthsLeft > 0) {
                    if(monthsLeft == 1) {
                        endTime = nowTime
                    } else {
                        endTime = startTime.plusMonths(1)
                    }

                    periods.add(
                        "${endTime.minusSeconds(1).format(DateTimeFormatter.ofPattern("MMM"))}\n'${endTime.minusSeconds(1).format(DateTimeFormatter.ofPattern("YY"))}" to (startTime.toInstant() to endTime.toInstant().minusSeconds(1))
                    )

                    startTime = endTime
                    monthsLeft--
                }

                return periods
            }

            else -> {
                var startTime = startOfDay(birthdate)
                var endTime = nowTime
                var daysBetween = (endTime.toEpochSecond() / Constants.Time.SECONDS_PER_DAY) - (startTime.toEpochSecond() / Constants.Time.SECONDS_PER_DAY)
                var periodsLeft = 12

                while(periodsLeft > 0) {

                    if(periodsLeft > 1) {
                        endTime = startTime.plusDays(daysBetween / periodsLeft)

                        periods.add(
                            "${startTime.minusSeconds(1).format(DateTimeFormatter.ofPattern("MMM"))}\n'${endTime.minusSeconds(1).format(DateTimeFormatter.ofPattern("YY"))}" to (startTime.toInstant() to endTime.toInstant().minusSeconds(1))
                        )

                        startTime = endTime
                        daysBetween -= daysBetween / periodsLeft
                    } else {
                        endTime = nowTime

                        periods.add(
                            "${startTime.minusSeconds(1).format(DateTimeFormatter.ofPattern("MMM"))}\n'${endTime.minusSeconds(1).format(DateTimeFormatter.ofPattern("YY"))}" to (startTime.toInstant() to endTime.toInstant().minusSeconds(1))
                        )

                        break
                    }

                    periodsLeft--
                }

                return periods
            }
        }
    }

    fun fullDateShort(time: Instant) = time.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM-dd-YY"))

    fun getDateByLocale(timeInMills: Long, locale: Locale): String? {
        return SimpleDateFormat("MMM dd, yyyy hh:mm aa", locale).format(Date(timeInMills))
    }

    fun getCsvDateByLocale(timeInMills: Long, locale: Locale): String? {
        return SimpleDateFormat("MMM dd yyyy hh:mm aa", locale).format(Date(timeInMills))
    }

    fun getShortHistoryDateByLocale(timeInMills: Long, locale: String?): String? {
        return SimpleDateFormat("MMM dd", Locale(locale)).format(Date(timeInMills))
    }

    fun getLongHistoryDateByLocale(timeInMills: Long, locale: String?): String? {
        return SimpleDateFormat("MMM dd, yyyy", Locale(locale)).format(Date(timeInMills))
    }

    fun getFileFormattedDate(timeInMills: Long, locale: Locale): String? {
        return SimpleDateFormat("MMM-dd-yyyy-hh", locale).format(Date(timeInMills))
    }
}
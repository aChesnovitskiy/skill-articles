package ru.skillbranch.skillarticles.extensions

import java.text.SimpleDateFormat
import java.util.*

const val SECOND = 1000L
const val MINUTE = 60 * SECOND
const val HOUR = 60 * MINUTE
const val DAY = 24 * HOUR

fun Date.format(pattern: String = "HH:mm:ss dd.MM.yy"): String {
    val dateFormat = SimpleDateFormat(pattern, Locale("ru"))
    return dateFormat.format(this)
}

fun Date.add(value: Int, units: TimeUnits = TimeUnits.SECOND) : Date {
    var time = this.time
    time += when (units) {
        TimeUnits.SECOND -> value * SECOND
        TimeUnits.MINUTE -> value * MINUTE
        TimeUnits.HOUR -> value * HOUR
        TimeUnits.DAY -> value * DAY
    }
    this.time = time
    return this
}

fun Date.humanizeDiff(date: Date = Date()): String {
    var humanizeDiff = " "

    val diff = ((date.time)/1000 - (this.time)/1000).toInt() // Difference in seconds
    val diffMinute = diff / 60
    val diffHour = diff / 3600
    val diffDay = diff / 86400

    when {
        diff in -1..1 -> humanizeDiff = "только что"
        diff in 2..45 -> humanizeDiff = "несколько секунд назад"
        diff in 46..75 -> humanizeDiff = "минуту назад"
        diff in 76..2700 -> humanizeDiff = "${TimeUnits.MINUTE.plural(diffMinute)} назад"
        diff in 2701..4500 -> humanizeDiff = "час назад"
        diff in 4501..79200 -> humanizeDiff = "${TimeUnits.HOUR.plural(diffHour)} назад"
        diff in 79201..93600 -> humanizeDiff = "день назад"
        diff in 93601..31104000 -> humanizeDiff = "${TimeUnits.DAY.plural(diffDay)} назад"
        diff > 31104000 -> humanizeDiff = "более года назад"
        diff in -1 downTo -45 -> humanizeDiff = "через несколько секунд"
        diff in -46 downTo -75 -> humanizeDiff = "через минуту"
        diff in -76 downTo -2700 -> humanizeDiff = "через ${TimeUnits.MINUTE.plural(diffMinute)}"
        diff in -2701 downTo -4500 -> humanizeDiff = "через час"
        diff in -4501 downTo -79200 -> humanizeDiff = "через ${TimeUnits.HOUR.plural(diffHour)}"
        diff in -79201 downTo -93600 -> humanizeDiff = "через день"
        diff in -93601 downTo -31104000 -> humanizeDiff = "через ${TimeUnits.DAY.plural(diffDay)}"
        diff < -31104000 -> humanizeDiff = "более чем через год"
    }

    return humanizeDiff
}

fun Date.shortFormat(): String {
    return this.format(if (isSameDay(Date())) "HH:mm" else "dd.MM.yy")
}

fun Date.isSameDay(date: Date): Boolean {
    return this.time / DAY == date.time / DAY
}

enum class TimeUnits {
    SECOND,
    MINUTE,
    HOUR,
    DAY;

    fun plural(value: Int): String {
        var quantity = " "
        val endOfValue = StrictMath.abs(value) % 100
        var ending = 0
        var unitPlural = " "

        when {
            endOfValue in 0..19 -> ending = endOfValue
            endOfValue > 19 -> ending = endOfValue % 10
        }

        when (ending) {
            1 -> quantity = "one"
            2, 3, 4 -> quantity = "few"
            0, in 4..19 -> quantity = "many"
        }

        when (this) {
            SECOND -> when (quantity) {
                "one" -> unitPlural = "секунду"
                "few" -> unitPlural = "секунды"
                "many" -> unitPlural = "секунд"
            }
            MINUTE -> when (quantity) {
                "one" -> unitPlural = "минуту"
                "few" -> unitPlural = "минуты"
                "many" -> unitPlural = "минут"
            }
            HOUR -> when (quantity) {
                "one" -> unitPlural = "час"
                "few" -> unitPlural = "часа"
                "many" -> unitPlural = "часов"
            }
            DAY -> when (quantity) {
                "one" -> unitPlural = "день"
                "few" -> unitPlural = "дня"
                "many" -> unitPlural = "дней"
            }
        }

        return "${StrictMath.abs(value)} $unitPlural"
    }
}
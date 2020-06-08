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
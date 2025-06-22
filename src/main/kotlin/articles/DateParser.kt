package articles

import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.Month
import java.time.temporal.ChronoUnit

object DateParser {
    val logger = LoggerFactory.getLogger(DateParser::class.java)

    fun parseDate(date: String): LocalDate {
        return when {
            date.contains("ago") -> ago(date)
            date.split(" ").size == 2 -> thisYear(date)

            else -> givenDate(date)
        }

    }

    private fun givenDate(date: String): LocalDate {
        val split = date.split(" ")

        logger.info("date: $date")
        logger.info("split: $split")

        val month = month(split[0])
        val day = split[1].dropLast(1).toInt()
        val year = split[2].toInt()

        return LocalDate.of(year, month, day)
    }

    private fun thisYear(date: String): LocalDate {
        val split = date.split(" ")
        val year = now().year
        val month = month(split[0])
        val day = split[1].toInt()

        return LocalDate.of(year, month, day)
    }

    private fun ago(date: String): LocalDate {
        val ago = date.split(" ").first()
        val letter = ago.takeLast(1)
        val count = ago.dropLast(1).toLong()
        val unit = when (letter) {
            "d" -> ChronoUnit.DAYS
            "w" -> ChronoUnit.WEEKS
            else -> ChronoUnit.MONTHS
        }

        return now().minus(count, unit)
    }

    private fun month(mon: String): Month = when (mon) {
        "Jan" -> Month.JANUARY
        "Feb" -> Month.FEBRUARY
        "Mar" -> Month.MARCH
        "Apr" -> Month.APRIL
        "May" -> Month.MAY
        "Jun" -> Month.JUNE
        "Jul" -> Month.JULY
        "Aug" -> Month.AUGUST
        "Sep" -> Month.SEPTEMBER
        "Oct" -> Month.OCTOBER
        "Nov" -> Month.NOVEMBER
        else -> Month.DECEMBER
    }
}

package io.github.alxiw.punkpaging.util

import org.threeten.bp.Year
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException
import org.threeten.bp.format.TextStyle
import java.util.*

object DateFormatter {

    private const val EMPTY_PLACEHOLDER = "∅"

    private val MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy")
    private val YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy")

    fun formatDate(string: String?, short: Boolean): String {
        if (string == null) {
            return EMPTY_PLACEHOLDER
        }
        return try {
            YearMonth.parse(string, MONTH_YEAR_FORMATTER).run {
                val style = if (short) TextStyle.SHORT else TextStyle.FULL
                "${month.getDisplayName(style, Locale.US).toUpperCase(Locale.US)} $year"
            }
        } catch (e: DateTimeParseException) {
            try {
                Year.parse(string, YEAR_FORMATTER).run {
                    toString()
                }
            } catch (e: DateTimeParseException) {
                string
            }
        }
    }
}

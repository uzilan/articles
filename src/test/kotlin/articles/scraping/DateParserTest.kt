package articles.scraping

import articles.scraping.DateParser.parseDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDate.now
import java.time.Month

class DateParserTest {

    @Test
    fun `five days ago`() {
        val text = "5d ago"
        val expected = now().minusDays(5)

        assertThat(parseDate(text)).isEqualTo(expected)
    }

    @Test
    fun `one weeks ago`() {
        val text = "1w ago"
        val expected = now().minusWeeks(1)

        assertThat(parseDate(text)).isEqualTo(expected)
    }

    @Test
    fun `may 9 this year`() {
        val text = "May 9"
        val expected = LocalDate.of(now().year, Month.MAY, 9)

        assertThat(parseDate(text)).isEqualTo(expected)
    }

    @Test
    fun `april 27 this year`() {
        val text = "Apr 27"
        val expected = LocalDate.of(now().year, Month.APRIL, 27)

        assertThat(parseDate(text)).isEqualTo(expected)
    }

    @Test
    fun `november 26 2024`() {
        val text = "Nov 26, 2024"
        val expected = LocalDate.of(2024, Month.NOVEMBER, 26)

        assertThat(parseDate(text)).isEqualTo(expected)
    }

    @Test
    fun `August 17 2023`() {
        val text = "Aug 17, 2023"
        val expected = LocalDate.of(2023, Month.AUGUST, 17)

        assertThat(parseDate(text)).isEqualTo(expected)
    }
}

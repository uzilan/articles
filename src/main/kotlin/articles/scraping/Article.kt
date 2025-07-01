package articles.scraping

import java.time.LocalDate

data class Article(
    val title: String,
    val subtitle: String,
    val published: LocalDate,
    val publication: String,
    val link: String,
    val imageUrl: String,
    val imageAlt: String,
    val claps: Int,
    val responses: Int,
)

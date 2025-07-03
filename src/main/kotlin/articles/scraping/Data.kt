package articles.scraping

enum class Status {
    ONGOING,
    FINISHED,
}

data class Data(
    val name: String,
    val followers: String,
    val description: String,
    val articles: List<Article>,
    val status: Status,
)

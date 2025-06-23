package articles

import java.util.UUID

enum class Status {
    ONGOING, FINISHED
}

data class Data(
    val name: String,
    val followers: String,
    val description: String,
    val articles: List<Article>,
    val status: Status,
)

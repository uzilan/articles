package articles

data class Data(
    val name: String,
    val followers: String,
    val description: String,
    val articles: List<Article>,
)

package articles.embedding

sealed class Field(
    val value: String,
) {
    object Description : Field("description")

    object Name : Field("name")

    object Title : Field("title")

    object Subtitle : Field("subtitle")

    object Publication : Field("publication")
    // Add more fields as needed

    override fun toString() = value
}

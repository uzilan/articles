package articles.embedding

data class DataEmbeddings(
    val fields: FieldEmbeddingsMap,
    val articles: Map<Title, FieldEmbeddingsMap>,
)

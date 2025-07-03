package articles.embedding

import articles.rag.RagService
import articles.scraping.Data

class EmbeddingService(
    private val embeddingStrategy: EmbeddingStrategy,
    private val ragService: RagService? = null,
) {
    /**
     * Public method to embed text using the underlying strategy.
     */
    fun embed(text: String): List<Float> = embeddingStrategy.embed(text)

    /**
     * Generates embeddings for a Data object, returning a DataEmbeddings structure
     * and updates the ragService if present.
     */
    fun generateEmbeddings(data: Data): DataEmbeddings {
        val fields = mutableMapOf<Field, FieldEmbedding>()

        // Generate embedding for the description and name fields
        embedIfNotBlank(fields, Field.Description, data.description)
        embedIfNotBlank(fields, Field.Name, data.name)

        // Generate embeddings for article titles, subtitles, and publication
        val articles =
            data.articles
                .mapNotNull { article ->
                    val articleEmbedding = mutableMapOf<Field, FieldEmbedding>()

                    embedIfNotBlank(articleEmbedding, Field.Title, article.title)
                    embedIfNotBlank(articleEmbedding, Field.Subtitle, article.subtitle)
                    embedIfNotBlank(articleEmbedding, Field.Publication, article.publication)

                    if (articleEmbedding.isNotEmpty()) {
                        Title(article.title) to FieldEmbeddingsMap(articleEmbedding)
                    } else {
                        null
                    }
                }.toMap()

        val dataEmbeddings = DataEmbeddings(fields = FieldEmbeddingsMap(fields), articles = articles)
        ragService?.update(data, dataEmbeddings)
        return dataEmbeddings
    }

    /**
     * Helper method to embed text if it's not blank and add to the map
     */
    private fun embedIfNotBlank(
        map: MutableMap<Field, FieldEmbedding>,
        field: Field,
        value: String,
    ) {
        if (value.isNotBlank()) {
            map[field] = FieldEmbedding(field, embeddingStrategy.embed(value))
        }
    }
}

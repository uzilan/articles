package articles.rag

import articles.embedding.DataEmbeddings
import articles.embedding.EmbeddingService
import articles.embedding.Field
import articles.embedding.Title
import articles.scraping.Data
import kotlin.math.sqrt

// Holds a Data object and its embeddings
data class RagEntry(
    val data: Data,
    val embeddings: DataEmbeddings,
)

data class ArticleSearchResult(
    val ragEntry: RagEntry,
    val title: Title,
    val similarity: Float,
)

class RagService {
    private val ragStore = mutableListOf<RagEntry>()

    /**
     * Replace any existing RagEntry with the same Data.name, then add the new one.
     */
    fun update(
        data: Data,
        embeddings: DataEmbeddings,
    ) {
        ragStore.removeAll { it.data.name == data.name }
        ragStore.add(RagEntry(data, embeddings))
    }

    /**
     * Get all RAG entries
     */
    fun getAll(): List<RagEntry> = ragStore.toList()

    /**
     * Find a RAG entry by Data.name
     */
    fun findByName(name: String): RagEntry? = ragStore.find { it.data.name == name }

    /**
     * Semantic search by article title for a specific writer (by Data.name).
     * Returns the topK most similar articles (title and similarity) for that writer only.
     */
    fun semanticSearchArticlesOfWriter(
        writerName: String,
        query: String,
        embeddingService: EmbeddingService,
        topK: Int = 5,
    ): List<Pair<Title, Float>> {
        val entry = findByName(writerName) ?: return emptyList()
        val queryEmbedding = embeddingService.embed(query)
        return entry.embeddings.articles
            .mapNotNull { (title, fieldEmbeddingsMap) ->
                val titleEmbedding = fieldEmbeddingsMap.embeddings[Field.Title]?.embedding
                if (titleEmbedding != null) {
                    val similarity = cosineSimilarity(queryEmbedding, titleEmbedding)
                    title to similarity
                } else {
                    null
                }
            }.sortedByDescending { it.second }
            .take(topK)
    }

    private fun cosineSimilarity(
        a: List<Float>,
        b: List<Float>,
    ): Float {
        val dot = a.zip(b).sumOf { (x, y) -> (x * y).toDouble() }
        val normA = sqrt(a.sumOf { (it * it).toDouble() })
        val normB = sqrt(b.sumOf { (it * it).toDouble() })
        return if (normA == 0.0 || normB == 0.0) 0f else (dot / (normA * normB)).toFloat()
    }
}

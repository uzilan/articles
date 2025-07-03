package articles.embedding

import articles.scraping.Data

// Holds a Data object and its embeddings
data class RagEntry(
    val data: Data,
    val embeddings: DataEmbeddings,
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
}

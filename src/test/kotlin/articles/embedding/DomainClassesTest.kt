package articles.embedding

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DomainClassesTest {
    @Test
    fun `Field sealed class only allows predefined fields`() {
        assertThat(Field.Description.value).isEqualTo("description")
        assertThat(Field.Name.value).isEqualTo("name")
        assertThat(Field.Title.value).isEqualTo("title")
        assertThat(Field.Subtitle.value).isEqualTo("subtitle")
        assertThat(Field.Publication.value).isEqualTo("publication")
    }

    @Test
    fun `Title stores and returns value correctly`() {
        val title = Title("My Article")
        assertThat(title.value).isEqualTo("My Article")
    }

    @Test
    fun `FieldEmbedding stores field and embedding`() {
        val field = Field.Name
        val embedding = listOf(0.1f, 0.2f, 0.3f)
        val fieldEmbedding = FieldEmbedding(field, embedding)
        assertThat(fieldEmbedding.field).isEqualTo(Field.Name)
        assertThat(fieldEmbedding.embedding).containsExactly(0.1f, 0.2f, 0.3f)
    }

    @Test
    fun `FieldEmbeddingsMap stores and retrieves embeddings by field`() {
        val nameEmbedding = FieldEmbedding(Field.Name, listOf(1.0f, 2.0f))
        val descEmbedding = FieldEmbedding(Field.Description, listOf(3.0f, 4.0f))
        val map = FieldEmbeddingsMap(mapOf(Field.Name to nameEmbedding, Field.Description to descEmbedding))
        assertThat(map.embeddings[Field.Name]).isEqualTo(nameEmbedding)
        assertThat(map.embeddings[Field.Description]).isEqualTo(descEmbedding)
    }

    @Test
    fun `DataEmbeddings stores fields and articles correctly`() {
        val fields = FieldEmbeddingsMap(mapOf(Field.Name to FieldEmbedding(Field.Name, listOf(1.0f))))
        val articleTitle = Title("A")
        val articleFields = FieldEmbeddingsMap(mapOf(Field.Title to FieldEmbedding(Field.Title, listOf(2.0f))))
        val articles = mapOf(articleTitle to articleFields)
        val dataEmbeddings = DataEmbeddings(fields, articles)
        assertThat(dataEmbeddings.fields.embeddings[Field.Name]?.embedding).containsExactly(1.0f)
        assertThat(dataEmbeddings.articles[articleTitle]?.embeddings?.get(Field.Title)?.embedding).containsExactly(2.0f)
    }
}

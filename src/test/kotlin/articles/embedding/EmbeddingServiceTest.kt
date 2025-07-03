package articles.embedding

import articles.scraping.Article
import articles.scraping.Data
import articles.scraping.Status
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class EmbeddingServiceTest {
    private val embeddingService = EmbeddingService(FakeEmbeddingStrategy())

    @Test
    fun `generates embeddings for Data fields and articles`() {
        val article1 =
            Article(
                title = "First Article",
                subtitle = "Intro",
                published = LocalDate.of(2023, 1, 1),
                publication = "Medium",
                link = "https://example.com/1",
                imageUrl = "",
                imageAlt = "",
                claps = 10,
                responses = 2,
            )
        val article2 =
            Article(
                title = "",
                subtitle = "",
                published = LocalDate.of(2023, 1, 2),
                publication = "",
                link = "https://example.com/2",
                imageUrl = "",
                imageAlt = "",
                claps = 5,
                responses = 1,
            )
        val data =
            Data(
                name = "Test Name",
                followers = "1000",
                description = "Test Description",
                articles = listOf(article1, article2),
                status = Status.ONGOING,
            )

        val result = embeddingService.generateEmbeddings(data)

        // Top-level fields
        assertThat(result.fields.embeddings[Field.Name]?.embedding).hasSize(384) // Fake embedding size
        assertThat(result.fields.embeddings[Field.Description]?.embedding).hasSize(384) // Fake embedding size

        // Article 1 fields
        val title1 = Title(article1.title)
        val article1Embeddings = result.articles[title1]!!.embeddings
        assertThat(article1Embeddings[Field.Title]?.embedding).hasSize(384) // Fake embedding size
        assertThat(article1Embeddings[Field.Subtitle]?.embedding).hasSize(384) // Fake embedding size
        assertThat(article1Embeddings[Field.Publication]?.embedding).hasSize(384) // Fake embedding size

        // Article 2 should be skipped (all blank fields)
        val title2 = Title(article2.title)
        assertThat(result.articles[title2]).isNull()
    }

    @Test
    fun `skips blank Data fields`() {
        val data =
            Data(
                name = "",
                followers = "1000",
                description = "",
                articles = emptyList(),
                status = Status.ONGOING,
            )
        val result = embeddingService.generateEmbeddings(data)
        assertThat(result.fields.embeddings).isEmpty()
        assertThat(result.articles).isEmpty()
    }
}

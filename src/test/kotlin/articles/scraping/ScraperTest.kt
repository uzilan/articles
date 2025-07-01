package articles.scraping

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.LocalDate
import com.microsoft.playwright.Browser
import com.microsoft.playwright.ElementHandle
import com.microsoft.playwright.Page

class ScraperTest {

    @Test
    fun `parseItem returns correct Article for valid ElementHandle`() {
        // Arrange: create a mock ElementHandle with expected structure
        val mockBrowser = mockk<Browser>(relaxed = true)
        val scraper = Scraper(mockBrowser)
        val mockHandle = mockk<ElementHandle>(relaxed = true)
        val parent = mockk<ElementHandle>(relaxed = true)
        val grandParent = mockk<ElementHandle>(relaxed = true)
        val greatGrandParent = mockk<ElementHandle>(relaxed = true)
        val uncle = mockk<ElementHandle>(relaxed = true)

        // Set up the element hierarchy
        every { mockHandle.evaluateHandle(any()) } returns parent
        every { parent.evaluateHandle(any()) } returns grandParent
        every { grandParent.evaluateHandle(any()) } returns greatGrandParent
        every { greatGrandParent.evaluateHandle(any()) } returns uncle
        every { uncle.innerText() } returns "PublicationName"
        every { grandParent.innerText() } returns "Title\nSubtitle\nMay 9, 2024\n100\n5"
        every { greatGrandParent.querySelectorAll("img") } returns listOf(mockk<ElementHandle> {
            every { getAttribute("src") } returns "https://image.url/test.jpg"
            every { getAttribute("alt") } returns "Alt text"
        })
        every { mockHandle.getAttribute("href") } returns "/p/test-article"

        // Act
        val article = scraper.parseItem(mockHandle)

        // Assert
        assertThat(article.title).isEqualTo("Title")
        assertThat(article.subtitle).isEqualTo("Subtitle")
        assertThat(article.published).isEqualTo(LocalDate.of(2024, 5, 9))
        assertThat(article.publication).isEqualTo("PublicationName")
        assertThat(article.link).isEqualTo("https://medium.com/p/test-article")
        assertThat(article.imageUrl).contains("image.url/test.jpg")
        assertThat(article.imageAlt).isEqualTo("Alt text")
        assertThat(article.claps).isEqualTo(100)
        assertThat(article.responses).isEqualTo(5)
    }

    @Test
    fun `articles returns list of Article objects for page with multiple links`() {
        val mockBrowser = mockk<Browser>(relaxed = true)
        val scraper = Scraper(mockBrowser)
        val mockPage = mockk<Page>(relaxed = true)
        val alias = "@testuser"

        // Create two mock ElementHandles for <a><h2>...</h2></a>
        val h2Handle1 = mockk<ElementHandle>(relaxed = true)
        val h2Handle2 = mockk<ElementHandle>(relaxed = true)
        val parent1 = mockk<ElementHandle>(relaxed = true)
        val parent2 = mockk<ElementHandle>(relaxed = true)

        // Each h2 evaluates to its parent
        every { h2Handle1.evaluateHandle(any()) } returns parent1
        every { h2Handle2.evaluateHandle(any()) } returns parent2

        // Set up parseItem for each parent
        // We'll mock parseItem to return a simple Article for each
        val article1 = Article("Title1", "Subtitle1", LocalDate.of(2024, 5, 9), "Pub1", "https://medium.com/p/1", "img1", "alt1", 10, 1)
        val article2 = Article("Title2", "Subtitle2", LocalDate.of(2024, 5, 10), "Pub2", "https://medium.com/p/2", "img2", "alt2", 20, 2)

        // Mock the internals of parseItem by mocking the parent chain and innerText for each
        every { parent1.evaluateHandle(any()) } returns parent1
        every { parent2.evaluateHandle(any()) } returns parent2
        every { parent1.innerText() } returns "Title1\nSubtitle1\nMay 9, 2024\n10\n1"
        every { parent2.innerText() } returns "Title2\nSubtitle2\nMay 10, 2024\n20\n2"
        every { parent1.evaluateHandle(any()) } returns parent1
        every { parent2.evaluateHandle(any()) } returns parent2
        every { parent1.getAttribute("href") } returns "/p/1"
        every { parent2.getAttribute("href") } returns "/p/2"
        every { parent1.querySelectorAll("img") } returns listOf(mockk<ElementHandle> {
            every { getAttribute("src") } returns "img1"
            every { getAttribute("alt") } returns "alt1"
        })
        every { parent2.querySelectorAll("img") } returns listOf(mockk<ElementHandle> {
            every { getAttribute("src") } returns "img2"
            every { getAttribute("alt") } returns "alt2"
        })
        every { parent1.evaluateHandle(any()) } returns parent1
        every { parent2.evaluateHandle(any()) } returns parent2
        every { parent1.innerText() } returns "Title1\nSubtitle1\nMay 9, 2024\n10\n1"
        every { parent2.innerText() } returns "Title2\nSubtitle2\nMay 10, 2024\n20\n2"
        every { parent1.getAttribute("href") } returns "/p/1"
        every { parent2.getAttribute("href") } returns "/p/2"

        // Mock the page to return the two h2 handles
        every { mockPage.querySelectorAll("a > h2") } returns listOf(h2Handle1, h2Handle2)

        // Act
        val articles = scraper.articles(alias, mockPage)

        // Assert
        assertThat(articles).hasSize(2)
        assertThat(articles[0].title).isEqualTo("Title1")
        assertThat(articles[1].title).isEqualTo("Title2")
    }
} 
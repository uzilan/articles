package articles.scraping

import articles.embedding.EmbeddingService
import com.github.benmanes.caffeine.cache.Cache
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.ElementHandle
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.LoadState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class ScraperWorker(
    private val embeddingService: EmbeddingService,
    private val cache: Cache<String, Data>,
    private val parsedLinks: MutableMap<String, List<ElementHandle>>,
    private val activeScrapes: MutableMap<String, AtomicBoolean>,
) {
    private val logger: Logger = LoggerFactory.getLogger(ScraperWorker::class.java)

    fun scrape(
        alias: String,
        page: Page,
        browser: Browser?,
        context: BrowserContext?,
    ): Data? {
        try {
            val url = "https://medium.com/$alias"
            logger.info("*********************")
            logger.info("Fetching $url")
            logger.info("*********************")

            page.setDefaultTimeout(60_000.0)
            page.onConsoleMessage { msg ->
                // Only log console messages that are not locale-related errors
                if (!msg.text().contains("Incorrect locale information provided")) {
                    logger.info("[Console] ${msg.text()}")
                }
            }
            page.onRequestFailed { req -> logger.error("[Request Failed] ${req.url()}") }
            page.onResponse { res -> logger.info("[Response] ${res.status()} ${res.url()}") }

            // Add error handling for page errors
            page.onPageError { error ->
                if (!error.contains("Incorrect locale information provided")) {
                    logger.error("[Page Error] $error")
                }
            }

            page.navigate(url)
            page.waitForLoadState(LoadState.DOMCONTENTLOADED)

            // Wait a bit more for JavaScript to fully load and stabilize
            Thread.sleep(2000)

            val spans = page.querySelectorAll("span")
            logger.info("Found ${spans.size} elements")

            // Check if we have enough elements to extract data
            if (spans.size < 6) {
                logger.warn("Not enough span elements found for $alias (found ${spans.size}, expected at least 6)")
                return null
            }

            // Add safety checks for element access
            val name = spans[5].innerText()
            val followers = if (spans.size > 6) spans[6].innerText() else "N/A"
            val description =
                try {
                    page.querySelectorAll("p > span").lastOrNull()?.innerText() ?: "N/A"
                } catch (e: Exception) {
                    logger.warn("Could not get description: ${e.message}")
                    "N/A"
                }
            val articles = articles(alias, page)

            // Validate that we got some meaningful data
            if (name.isBlank() || name == "N/A") {
                logger.warn("Could not extract name for $alias")
                return null
            }

            val data =
                Data(name, followers, description, articles, Status.ONGOING)
                    .also { cache.put(alias, it) }

            // Track that scraping is in progress
            val isComplete = AtomicBoolean(false)
            activeScrapes[alias] = isComplete

            thread {
                try {
                    scrollToBottom(page, alias)
                    val completeData = cache.getIfPresent(alias) ?: throw IllegalStateException("$alias does not exist")
                    cache.put(alias, completeData.copy(status = Status.FINISHED))
                    embeddingService.generateEmbeddings(completeData)
                } finally {
                    // Always close the page, context, and browser when background processing is done
                    try {
                        page.close()
                        context?.close()
                        browser?.close()
                    } catch (e: Exception) {
                        logger.warn("Error closing browser resources: ${e.message}")
                    } finally {
                        // Mark scraping as complete
                        isComplete.set(true)
                        activeScrapes.remove(alias)
                    }
                }
            }

            return data
        } catch (e: Exception) {
            logger.error("Error during scraping for $alias: ${e.message}")
            // Clean up resources if there was an error
            try {
                page.close()
                context?.close()
                browser?.close()
            } catch (closeError: Exception) {
                logger.warn("Error closing browser resources: ${closeError.message}")
            }
            return null
        }
    }

    fun articles(
        alias: String,
        page: Page,
    ): List<Article> {
        try {
            val links =
                page.querySelectorAll("a > h2")
                    .mapNotNull { h2 ->
                        h2.evaluateHandle("node => node.parentElement") as? ElementHandle
                    }
            val oldLinks = parsedLinks.getOrDefault(alias, emptyList())
            val linkTexts = oldLinks.map { it.innerText() }
            val newLinks =
                links.filter { link ->
                    link.innerText() !in linkTexts
                }
            parsedLinks[alias] = links

            return newLinks.mapNotNull {
                try {
                    parseItem(it)
                } catch (e: Exception) {
                    logger.error("Error parsing article $it", e)
                    null
                }
            }
        } catch (e: Exception) {
            logger.warn("Error getting articles for $alias: ${e.message}")
            return emptyList()
        }
    }

    fun parseItem(handle: ElementHandle): Article {
        val parent = handle.evaluateHandle("node => node.parentElement") as? ElementHandle
        val grandParent = parent?.evaluateHandle("node => node.parentElement") as? ElementHandle
        val greatGrandParent = grandParent?.evaluateHandle("node => node.parentElement") as? ElementHandle
        val uncle = greatGrandParent?.evaluateHandle("node => node.previousElementSibling") as? ElementHandle
        val split = grandParent?.innerText()?.split("\n")
        val uncleSplit = uncle?.innerText()?.split("\n")

        if (split == null) {
            return Article("N/A", "N/A", LocalDate.EPOCH, "N/A", "N/A", "N/A", "N/A", 0, 0)
        }

        val map =
            greatGrandParent?.querySelectorAll("img")
                ?.map { it.getAttribute("src") to it.getAttribute("alt") }

        val (img, alt) =
            when {
                map == null || map.isEmpty() -> ("N/A" to "N/A")
                else -> map[0]
            }
        val imageUrl = img.replace(Regex("/resize:[^/]+"), "")

        val date =
            if (split[2].contains("ago") ||
                split[2].contains(", 20") ||
                split[2].contains("""[A-Z][a-z]{2}\s\d""".toRegex())
            ) {
                split[2]
            } else {
                split[1]
            }

        return Article(
            title = split.first(),
            subtitle = split[1],
            published = DateParser.parseDate(date),
            publication = uncleSplit?.last() ?: "N/A",
            link = "https://medium.com" + handle.getAttribute("href"),
            imageUrl = imageUrl,
            imageAlt = alt,
            claps = if (split.size >= 4) claps(split[3]) else 0,
            responses = if (split.size == 5) split[4].toInt() else 0,
        )
    }

    private fun claps(claps: String): Int {
        return if (claps.endsWith("K")) {
            (claps.dropLast(1).toDouble() * 1000).toInt()
        } else {
            claps.toInt()
        }
    }

    private fun scrollToBottom(
        page: Page,
        alias: String,
        maxAttempts: Int = 100,
        delayMs: Int = 2000,
    ) {
        var previousHeight = -1
        var attempts = 0

        while (attempts < maxAttempts) {
            try {
                val currentHeight = page.evaluate("() => document.body.scrollHeight") as Int
                if (currentHeight == previousHeight) {
                    println("Reached bottom after $attempts scrolls.")
                    break
                }

                page.evaluate("() => window.scrollTo(0, document.body.scrollHeight)")
                Thread.sleep(delayMs.toLong())
                previousHeight = currentHeight
                attempts++

                val articles = articles(alias, page)
                cache.getIfPresent(alias)?.let { data ->
                    cache.put(alias, data.copy(articles = data.articles + articles))
                } ?: throw IllegalStateException("$alias does not exist")
            } catch (e: Exception) {
                logger.warn("Error during scrolling for $alias: ${e.message}")
                break
            }
        }
    }
}

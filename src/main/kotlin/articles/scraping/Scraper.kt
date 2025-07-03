package articles.scraping

import articles.embedding.EmbeddingService
import com.github.benmanes.caffeine.cache.Caffeine
import com.microsoft.playwright.Browser
import com.microsoft.playwright.Browser.NewContextOptions
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.ElementHandle
import com.microsoft.playwright.Playwright
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private const val USER_AGENT =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/114.0.0.0 Safari/537.36"

class Scraper(
    private val embeddingService: EmbeddingService,
) {
    private val logger: Logger = LoggerFactory.getLogger(Scraper::class.java)
    private val cache =
        Caffeine
            .newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .maximumSize(1000)
            .build<String, Data>()
    private val parsedLinks = mutableMapOf<String, List<ElementHandle>>()
    private val activeScrapes = mutableMapOf<String, AtomicBoolean>()
    private val worker = ScraperWorker(embeddingService, cache, parsedLinks, activeScrapes)

    fun fetch(
        alias: String,
        browser: Browser? = null,
    ): Data = cache.getIfPresent(alias) ?: createPageAndHelper(alias, browser)

    fun isScrapingComplete(alias: String): Boolean = activeScrapes[alias]?.get() ?: true

    private fun createPageAndHelper(
        alias: String,
        browser: Browser?,
    ): Data {
        var browserToUse = browser

        // Create browser if not provided
        if (browserToUse == null) {
            val playwright = Playwright.create()
            browserToUse =
                playwright.chromium().launch(
                    BrowserType
                        .LaunchOptions()
                        .setHeadless(true)
                        .setArgs(listOf("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu")),
                )
        }

        // Create context and page
        val context =
            browserToUse.newContext(
                NewContextOptions()
                    .setViewportSize(1280, 800)
                    .setUserAgent(
                        USER_AGENT,
                    ).setLocale("en-US")
                    .setTimezoneId("America/New_York")
                    .setJavaScriptEnabled(true)
                    .setIgnoreHTTPSErrors(true),
            )
        val page = context.newPage()

        // Delegate to worker for the actual scraping
        return worker.scrape(alias, page, browserToUse, context)
            ?: throw IllegalStateException("Failed to scrape data for $alias")
    }
}

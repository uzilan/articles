package articles

import com.github.benmanes.caffeine.cache.Caffeine
import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.ElementHandle
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.LoadState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

private const val USER_AGENT =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"

object Scraper {

    private val logger: Logger = LoggerFactory.getLogger(Scraper::class.java)

    private val cache = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.DAYS)
        .maximumSize(1000)
        .build<String, Data>()

    private val parsedLinks = mutableMapOf<String, List<ElementHandle>>()

    fun fetch(alias: String): Data {
        return cache.getIfPresent(alias) ?: helper(alias)
    }

    private fun helper(alias: String): Data {
        val url = "https://medium.com/$alias"
        val playwright = Playwright.create()

        val browser = playwright.chromium().launch(
            BrowserType.LaunchOptions().setHeadless(true)
        )

        val context = browser.newContext(
            Browser.NewContextOptions()
                .setViewportSize(1280, 800)
                .setUserAgent(USER_AGENT)
        )
        val page = context.newPage()
        page.setDefaultTimeout(60_000.0)
        page.onConsoleMessage { msg -> logger.info("[Console] ${msg.text()}") }
        page.onRequestFailed { req -> logger.error("[Request Failed] ${req.url()}") }
        page.onResponse { res -> logger.info("[Response] ${res.status()} ${res.url()}") }
        page.navigate(url)

        page.waitForLoadState(LoadState.DOMCONTENTLOADED)

        val name = page.querySelectorAll("span")[5].innerText()
        val followers = page.querySelectorAll("span")[6].innerText()
        val description = page.querySelectorAll("p > span").last().innerText()
        val articles = articles(alias, page)

        val data = Data(name, followers, description, articles, Status.ONGOING)
            .also { cache.put(alias, it) }

        thread {
            scrollToBottom(page, alias)
            val completeData = cache.getIfPresent(alias) ?: throw IllegalStateException("$alias does not exist")
            cache.put(alias, completeData.copy(status = Status.FINISHED))
            browser.close()
        }

        return data
    }

    private fun articles(alias: String, page: Page): List<Article> {
        val links = page.querySelectorAll("a > h2")
            .mapNotNull { h2 ->
                h2.evaluateHandle("node => node.parentElement") as? ElementHandle
            }
        val oldLinks = parsedLinks.getOrDefault(alias, emptyList())
        val linkTexts = oldLinks.map { it.innerText() }
        val newLinks = links.filter { link ->
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
    }

    private fun parseItem(handle: ElementHandle): Article {
        val parent = handle.evaluateHandle("node => node.parentElement") as? ElementHandle
        val grandParent = parent?.evaluateHandle("node => node.parentElement") as? ElementHandle
        val greatGrandParent = grandParent?.evaluateHandle("node => node.parentElement") as? ElementHandle
        val uncle = greatGrandParent?.evaluateHandle("node => node.previousElementSibling") as? ElementHandle
        val split = grandParent?.innerText()?.split("\n")
        val uncleSplit = uncle?.innerText()?.split("\n")

        if (split == null) {
            return Article("N/A", "N/A", LocalDate.EPOCH, "N/A", "N/A", "N/A", "N/A", 0, 0)
        }

        val map = greatGrandParent?.querySelectorAll("img")
            ?.map { it.getAttribute("src") to it.getAttribute("alt") }

        val (img, alt) = when {
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
            } else split[1]

        return Article(
            title = split.first(),
            subtitle = split[1],
            published = DateParser.parseDate(date),
            publication = uncleSplit?.last() ?: "N/A",
            link = "https://medium.com" + handle.getAttribute("href"),
            imageUrl = imageUrl,
            imageAlt = alt,
            claps = if (split.size >= 4) claps(split[3]) else 0,
            responses = if (split.size == 5) split[4].toInt() else 0
        )
    }

    private fun claps(claps: String): Int {
        return if (claps.endsWith("K")) (claps.dropLast(1).toDouble() * 1000).toInt()
        else claps.toInt()
    }

    fun scrollToBottom(page: Page, alias: String, maxAttempts: Int = 100, delayMs: Int = 2000) {
        var previousHeight = -1
        var attempts = 0

        while (attempts < maxAttempts) {
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
        }
    }
}

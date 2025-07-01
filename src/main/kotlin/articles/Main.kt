package articles

import articles.scraping.Data
import articles.scraping.Scraper
import com.fasterxml.jackson.databind.SerializationFeature
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.json.JavalinJackson
import io.javalin.openapi.HttpMethod
import io.javalin.openapi.OpenApi
import io.javalin.openapi.OpenApiContent
import io.javalin.openapi.OpenApiParam
import io.javalin.openapi.OpenApiResponse
import io.javalin.openapi.plugin.OpenApiPlugin
import io.javalin.openapi.plugin.swagger.SwaggerConfiguration
import io.javalin.openapi.plugin.swagger.SwaggerPlugin
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright

object Main {
    private val port = System.getenv("PORT")?.toIntOrNull() ?: 7070
    private const val USER_AGENT =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"

    @JvmStatic
    fun main(args: Array<String>) {
        val playwright = Playwright.create()
        val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
        val scraper = Scraper(browser)

        Runtime.getRuntime().addShutdownHook(Thread {
            browser.close()
        })

        Javalin.create { config ->
            config.staticFiles.add(
                directory = "/public",
                location = Location.CLASSPATH,
            )
            config.bundledPlugins.enableCors { cors ->
                cors.addRule {
                    it.anyHost()
                }
            }
            config.jsonMapper(JavalinJackson().updateMapper { mapper ->
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            })
            config.registerPlugin(OpenApiPlugin { openApiConfig ->
                openApiConfig
                    .withDocumentationPath("/openapi")
                    .withDefinitionConfiguration { _, openApiDefinition ->
                        openApiDefinition.withInfo { info ->
                            info.title = "Medium Scraper API"
                            info.description = "API for scraping Medium user/publication data."
                        }
                    }
            })
            config.registerPlugin(SwaggerPlugin { SwaggerConfiguration() })
        }
            .get("/scrap/{alias}") { ctx ->
                val alias = ctx.pathParam("alias")
                val aliasWithAt1 = if (alias.startsWith("@")) alias else "@$alias"
                val aliasWithAt = aliasWithAt1
                val context = browser.newContext(
                    com.microsoft.playwright.Browser.NewContextOptions()
                        .setViewportSize(1280, 800)
                        .setUserAgent(USER_AGENT)
                )
                val page = context.newPage()
                val scrap = scraper.fetch(aliasWithAt, page)
                ctx.json(scrap)
            }
            .start(port)
    }

    @OpenApi(
        summary = "Scrape Medium profile by alias",
        path = "/scrap/{alias}",
        methods = [HttpMethod.GET],
        pathParams = [OpenApiParam(name = "alias", description = "Medium user or publication alias")],
        responses = [OpenApiResponse("200", [OpenApiContent(from = articles.scraping.Data::class)])]
    )
    fun scrapHandler(ctx: io.javalin.http.Context) { /* not used, kept for OpenAPI */ }
}

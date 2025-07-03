package articles

import articles.embedding.EmbeddingService
import articles.embedding.FakeEmbeddingStrategy
import articles.scraping.Scraper
import com.fasterxml.jackson.databind.SerializationFeature
import com.microsoft.playwright.Playwright
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

object Main {
    private val port = System.getenv("PORT")?.toIntOrNull() ?: 7070

    @JvmStatic
    fun main(args: Array<String>) {
        val playwright = Playwright.create()
        val embeddingStrategy = FakeEmbeddingStrategy()
        val embeddingService = EmbeddingService(embeddingStrategy)
        val scraper = Scraper(embeddingService)

        Runtime.getRuntime().addShutdownHook(
            Thread {
                try {
                    playwright.close()
                } catch (e: Exception) {
                    println("Error during shutdown: ${e.message}")
                }
            },
        )

        Javalin
            .create { config ->
                config.staticFiles.add(
                    directory = "/public",
                    location = Location.CLASSPATH,
                )
                config.bundledPlugins.enableCors { cors ->
                    cors.addRule {
                        it.anyHost()
                    }
                }
                config.jsonMapper(
                    JavalinJackson().updateMapper { mapper ->
                        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    },
                )
                config.registerPlugin(
                    OpenApiPlugin { openApiConfig ->
                        openApiConfig
                            .withDocumentationPath("/openapi")
                            .withDefinitionConfiguration { _, openApiDefinition ->
                                openApiDefinition.withInfo { info ->
                                    info.title = "Medium Scraper API"
                                    info.description = "API for scraping Medium user/publication data."
                                }
                            }
                    },
                )
                config.registerPlugin(SwaggerPlugin { SwaggerConfiguration() })
            }.get("/health") { ctx ->
                ctx.json(mapOf("status" to "ok", "browser" to "available"))
            }.get("/status/{alias}") { ctx ->
                val alias = ctx.pathParam("alias")
                val aliasWithAt = if (alias.startsWith("@")) alias else "@$alias"
                val isComplete = scraper.isScrapingComplete(aliasWithAt)
                ctx.json(mapOf("alias" to aliasWithAt, "complete" to isComplete))
            }.get("/scrap/{alias}") { ctx ->
                val alias = ctx.pathParam("alias")
                val aliasWithAt1 = if (alias.startsWith("@")) alias else "@$alias"
                val aliasWithAt = aliasWithAt1

                try {
                    // Let the scraper create its own browser, context, and page
                    val data = scraper.fetch(aliasWithAt)
                    ctx.json(data)
                } catch (e: Exception) {
                    println("Error during scraping: ${e.message}")
                    e.printStackTrace()
                    ctx.status(500).json(mapOf("error" to "Failed to scrape data: ${e.message}"))
                }
            }.start(port)
    }

    @OpenApi(
        summary = "Scrape Medium profile by alias",
        path = "/scrap/{alias}",
        methods = [HttpMethod.GET],
        pathParams = [OpenApiParam(name = "alias", description = "Medium user or publication alias")],
        responses = [OpenApiResponse("200", [OpenApiContent(from = articles.scraping.Data::class)])],
    )
    fun scrapHandler(ctx: io.javalin.http.Context) { /* not used, kept for OpenAPI */ }
}

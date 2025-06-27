package articles

import com.fasterxml.jackson.databind.SerializationFeature
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.json.JavalinJackson

object Main {
    private val port = System.getenv("PORT")?.toIntOrNull() ?: 7070

    @JvmStatic
    fun main(args: Array<String>) {

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
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            })
        }
            .get("/scrap/{alias}") { ctx ->
                val alias = ctx.pathParam("alias")
                val aliasWithAt1 = if (alias.startsWith("@")) alias else "@$alias"
                val aliasWithAt = aliasWithAt1
                val scrap = Scraper.fetch(aliasWithAt)
                ctx.json(scrap)
            }
            .start(port)
    }

}

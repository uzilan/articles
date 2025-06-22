package articles

import com.fasterxml.jackson.databind.SerializationFeature
import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.json.JavalinJackson

object Main {
    @JvmStatic
    fun main(args: Array<String>) {

        Javalin.create { config ->
            config.staticFiles.add("ui/dist", Location.EXTERNAL)
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
                val aliasWithAt = if (alias.startsWith("@")) alias else "@$alias"
                val scrap = Scraper.scrap("https://medium.com/$aliasWithAt")
                ctx.json(scrap)
            }
            .start(7070)
    }

}

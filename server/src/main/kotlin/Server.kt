import com.beust.klaxon.Klaxon
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    val port = 8080;
    val host = "0.0.0.0";

    embeddedServer(Netty, port = port, host = host) {

        val exchangeAdmin = ExchangeAdmin()

        class ParameterNotFoundException(name: String) : Exception("Expected $name")

        routing {

            get("/get-item") {
                try {
                    val parameters = call.request.queryParameters
                    call.respond(
                        HttpStatusCode.OK,
                        Klaxon().toJsonString(
                            exchangeAdmin.getItemByTicker(
                                parameters["ticker"] ?: throw ParameterNotFoundException("ticker")
                            )
                        )
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }

            post("/add-item") {
                try {
                    val parameters = call.request.queryParameters
                    exchangeAdmin.addItem(
                        parameters["ticker"] ?: throw ParameterNotFoundException("ticker"),
                        (parameters["number"] ?: throw ParameterNotFoundException("number")).toInt()
                    )
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }

            get("/get-items") {
                try {
                    call.respond(
                        HttpStatusCode.OK,
                        Klaxon().toJsonString(
                            exchangeAdmin.getTickerToItemMap()
                        )
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }

            post("/buy-item") {
                try {
                    val parameters = call.request.queryParameters
                    call.respond(
                        HttpStatusCode.OK,
                        exchangeAdmin.buyItems(
                            parameters["ticker"] ?: throw ParameterNotFoundException("ticker"),
                            (parameters["number"] ?: throw ParameterNotFoundException("number")).toInt()
                        ).toString()
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }

            post("/set-price") {
                try {
                    val parameters = call.request.queryParameters
                    exchangeAdmin.setPrice(
                        parameters["ticker"] ?: throw ParameterNotFoundException("ticker"),
                        (parameters["price"] ?: throw ParameterNotFoundException("price")).toDouble()
                    )
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }

            post("/add-ticker") {
                try {
                    val parameters = call.request.queryParameters
                    exchangeAdmin.addTicker(
                        parameters["ticker"] ?: throw ParameterNotFoundException("ticker"),
                        (parameters["price"] ?: throw ParameterNotFoundException("price")).toDouble()
                    )
                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, e.message ?: "")
                }
            }
        }
    }.start(wait = true)
}

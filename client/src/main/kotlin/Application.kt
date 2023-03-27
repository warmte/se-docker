import com.beust.klaxon.Klaxon
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import model.Account
import model.ExchangeClient
import model.Item
import model.User

fun main() {
    val port = 8081;
    val host = "0.0.0.0";
    embeddedServer(Netty, port = port, host = host) { module1() }.start(wait = true)
}

fun Application.module1() {

    val account = Account()
    val exchangeClient = ExchangeClient("http://0.0.0.0:8080", account)

    class ParameterNotFoundException(name: String) : Exception("Parameter $name not found")

    suspend fun getUserPaperCurrencies(user: User): List<Item> {
        val userItemMap = user.items.map {
            val item = exchangeClient.get(it.key)
            item.number = it.value
            item
        }
        return userItemMap
    }

    routing {

        get("/get-item") {
            try {
                val parameters = call.request.queryParameters
                call.respond(
                    HttpStatusCode.OK,
                    Klaxon().toJsonString(
                        getUserPaperCurrencies(
                            account.getUser((parameters["id"] ?: throw ParameterNotFoundException("id")).toInt())
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }

        post("/add-user") {
            try {
                val parameters = call.request.queryParameters
                call.respond(
                    HttpStatusCode.OK,
                    account.createUser(parameters["name"] ?: throw ParameterNotFoundException("name")).toString()
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }

        post("/add-funds") {
            try {
                val parameters = call.request.queryParameters
                account.addFunds(
                    (parameters["id"] ?: throw ParameterNotFoundException("id")).toInt(),
                    (parameters["amount"] ?: throw ParameterNotFoundException("amount")).toDouble()
                )
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }

        get("/get-all-funds") {
            try {
                val parameters = call.request.queryParameters
                val user = account.getUser((parameters["id"] ?: throw ParameterNotFoundException("id")).toInt())
                call.respond(
                    HttpStatusCode.OK,
                    (getUserPaperCurrencies(user).sumOf { it.number * it.price } + user.funds).toString())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }

        post("/buy") {
            try {
                val parameters = call.request.queryParameters
                exchangeClient.buy(
                    (parameters["id"] ?: throw ParameterNotFoundException("id")).toInt(),
                    (parameters["ticker"] ?: throw ParameterNotFoundException("ticker")),
                    (parameters["number"] ?: throw ParameterNotFoundException("number")).toInt()
                )
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }

        post("/sell") {
            try {
                val parameters = call.request.queryParameters
                exchangeClient.sell(
                    (parameters["id"] ?: throw ParameterNotFoundException("id")).toInt(),
                    (parameters["ticker"] ?: throw ParameterNotFoundException("ticker")),
                    (parameters["number"] ?: throw ParameterNotFoundException("number")).toInt()
                )
                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "")
            }
        }
    }
}

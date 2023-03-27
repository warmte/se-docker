import com.beust.klaxon.Klaxon
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import model.Item
import org.testcontainers.containers.FixedHostPortGenericContainer
import kotlin.test.*

class Tests {

    private val port = 8080
    private val client = HttpClient(CIO)
    private val server = "http://localhost:$port"

    class MyFixedHostPortGenericContainer(name: String) :
        FixedHostPortGenericContainer<MyFixedHostPortGenericContainer>(name)

    private val container =
        MyFixedHostPortGenericContainer("server")
            .withFixedExposedPort(port, port)
            .withExposedPorts(port)

    @BeforeTest
    fun setUp() {
        container.start()
        runBlocking {
            client.post<HttpResponse>(
                "$server/add-ticker?ticker=${Tickers.APPL}&price=2000"
            )
            client.post<HttpResponse>(
                "$server/add-item?ticker=${Tickers.APPL}&number=100"
            )
            client.post<HttpResponse>(
                "$server/add-ticker?ticker=${Tickers.YNDX}&price=1000"
            )
            client.post<HttpResponse>(
                "$server/add-item?ticker=${Tickers.YNDX}&number=100"
            )
        }
    }

    @AfterTest
    fun tearDown() {
        container.stop()
    }

    @Test
    fun buyTest() {
        withTestApplication(Application::module1) {
            val userId = addUser(4000.0)
            handleRequest(HttpMethod.Post, "/buy?id=$userId&ticker=${Tickers.APPL}&number=1").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            handleRequest(HttpMethod.Post, "/buy?id=$userId&ticker=${Tickers.YNDX}&number=2").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
            checkItems(userId, listOf(
                    Item(Tickers.APPL, 2000.0, 1),
                    Item(Tickers.YNDX, 1000.0, 2)
                )
            )
            checkWealth(userId, 4000.0)
        }
    }

    @Test
    fun buyChangedTest() {
        withTestApplication({ module1() }) {
            runBlocking {
                val userId = addUser(2000.0)
                handleRequest(HttpMethod.Post, "/buy?id=$userId&ticker=${Tickers.APPL}&number=1").apply {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
                checkItems(userId, listOf(Item(Tickers.APPL, 2000.0, 1)))
                checkWealth(userId, 2000.0)
                HttpClient(CIO).post<String>("$server/set-price?ticker=${Tickers.APPL}&price=3000")
                checkItems(userId, listOf(Item(Tickers.APPL, 3000.0, 1)))
                checkWealth(userId, 3000.0)
            }
        }
    }

    private fun TestApplicationEngine.addUser(funds: Double = 0.0): Int {
        var user: Int
        handleRequest(HttpMethod.Post, "/add-user?name=John").apply {
            user = response.content!!.toInt()
        }
        handleRequest(HttpMethod.Post, "/add-funds?id=$user&amount=$funds")
        return user
    }

    private fun TestApplicationEngine.checkItems(userId: Int, expected: List<Item>) {
        handleRequest(HttpMethod.Get, "/get-item?id=$userId"
        ).apply {
            val items = Klaxon().parseArray<Item>(response.content!!)!!
            assertEquals(expected.size, items.size)
            assertTrue(expected.containsAll(items))
            assertTrue(items.containsAll(expected))
        }
    }

    private fun TestApplicationEngine.checkWealth(userId: Int, expected: Double) {
        handleRequest(HttpMethod.Get, "/get-all-funds?id=$userId").apply {
            assertEquals(expected, response.content!!.toDouble())
        }
    }
}
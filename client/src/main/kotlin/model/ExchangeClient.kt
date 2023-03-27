package model

import com.beust.klaxon.Klaxon
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class ExchangeClient(private val url: String, private val account: Account) {
    private val client = HttpClient(CIO)

    suspend fun get(ticker: String): Item {
        val json = client.get<String>("$url/get-item?ticker=$ticker")
        return Klaxon().parse<Item>(json)!!
    }

    suspend fun buy(id: Int, ticker: String, number: Int) {
        val item = get(ticker)
        if (item.number < number) {
            throw Account.AccountException("Not enough items")
        }
        account.removeFunds(id, item.price * number)
        client.post<HttpResponse>("$url/buy-item?ticker=$ticker&number=$number")
        account.addItem(id, ticker, number)
    }

    suspend fun sell(id: Int, ticker: String, number: Int) {
        val item = get(ticker)
        account.removeItems(id, ticker, number)
        client.post<HttpResponse>("$url/add-item?ticker=$ticker&number=$number")
        account.addFunds(id, item.price * number)
    }
}

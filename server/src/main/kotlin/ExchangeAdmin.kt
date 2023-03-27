import java.util.concurrent.ConcurrentHashMap

class ExchangeAdmin {
    class ServerException(name: String) : Exception(name)

    class Item(val ticker: String, var price: Double, var number: Int)

    private val tickerToItemMap: MutableMap<String, Item> = ConcurrentHashMap()

    fun setPrice(ticker: String, price: Double) {
        getItemByTicker(ticker).price = price
    }

    fun addTicker(ticker: String, price: Double) {
        if (ticker in tickerToItemMap) {
            throw ServerException("$ticker already added")
        }
        tickerToItemMap[ticker] = Item(ticker, price, 0)
    }

    fun getTickerToItemMap(): Map<String, Item> {
        return tickerToItemMap
    }

    fun getItemByTicker(ticker: String): Item {
        return tickerToItemMap[ticker] ?: throw ServerException("$ticker not found")
    }

    fun addItem(ticker: String, number: Int) {
        getItemByTicker(ticker).number += number
    }

    fun buyItems(company: String, quantity: Int): Double {
        val item = getItemByTicker(company)
        if (quantity > item.number) {
            throw ServerException("Not enough items")
        }
        item.number -= quantity
        return item.price * quantity
    }
}

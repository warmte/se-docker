package model

class Item(val ticker: String, var price: Double, var number: Int) {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (javaClass != other?.javaClass) {
            return false
        }
        other as Item
        if (ticker != other.ticker) {
            return false
        }
        if (price != other.price) {
            return false
        }
        if (number != other.number) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = ticker.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + number
        return result
    }

}

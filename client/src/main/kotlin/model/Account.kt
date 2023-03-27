package model

import java.util.concurrent.ConcurrentHashMap

class Account {
    class AccountException(name: String) : Exception(name)

    private val users: MutableMap<Int, User> = ConcurrentHashMap()

    fun addFunds(id: Int, amount: Double) {
        if (amount <= 0) {
            throw AccountException("Amount is negative")
        }
        val user = users[id] ?: throw AccountException("User $id not found")
        user.funds += amount
    }

    fun removeFunds(userId: Int, amount: Double) {
        val user = users[userId] ?: throw AccountException("User $userId not found")
        if (amount <= 0 || amount > user.funds) {
            throw AccountException("Insufficient funds")
        }
        user.funds -= amount
    }

    fun addItem(id: Int, ticker: String, number: Int) {
        val user = users[id] ?: throw AccountException("User $id not found")
        user.items[ticker] = user.items.getOrDefault(ticker, 0) + number
    }

    fun removeItems(id: Int, ticker: String, number: Int) {
        val user = users[id] ?: throw AccountException("User $id not found")
        if (user.items.getOrDefault(ticker, 0) < number) {
            throw AccountException("Insufficient funds")
        }
        user.items[ticker] = user.items[ticker]!! - number
    }

    fun createUser(userName: String): Int {
        val id = (users.keys.maxOrNull() ?: -1) + 1
        users[id] = User(id, userName, 0.0, mutableMapOf())
        return id
    }

    fun getUser(id: Int): User {
        return users[id] ?: throw AccountException("There is no user with id $id")
    }
}

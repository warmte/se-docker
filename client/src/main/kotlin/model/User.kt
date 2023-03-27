package model

class User(val id: Int, val name: String, var funds: Double, var items: MutableMap<String, Int>)
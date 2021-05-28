data class Item(val name: String, var price: Double, var count: Int)
class StoreRoom {
    val inventory: HashMap<String, Item> = HashMap()

    init {
        inventory["apple"] = Item("apple", 0.60, 10)
        inventory["orange"] = Item("orange", 0.25, 5)
    }

    fun getItem(name: String): Item? {
        return inventory[name]
    }

    fun takeItems(name: String, _count: Int): Boolean {
        val item: Item? = inventory[name]
        if (item != null && item.count >= _count) {
            item.count -= _count
            return true
        }
        return false
    }
}

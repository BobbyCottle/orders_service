import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import java.text.NumberFormat

class CottlinStore(_storeRoom: StoreRoom, _channel: SendChannel<ChannelMessage>) {
    private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance()
    private val storeRoom = _storeRoom
    private val scope = CoroutineScope(Job())
    private val channel = _channel

    class SpecialOffer(var itemsBought: Int, var itemsCharged: Int)

    var specials: MutableMap<String, SpecialOffer> = mutableMapOf()

    init {
        println("Welcome to the Cottlin Orders System!")
        specials["apple"] = SpecialOffer(2, 1)
        specials["orange"] = SpecialOffer(3, 2)
    }

    fun start() {
        println("Store: starting shop...")
        while (!channel.isClosedForSend) {
            val order = takeOrder()
            if (order.isEmpty()) {
                channel.close()
                break
            }
            processOrder(order)
        }
        println("Store: The Cottlin Orders System is now closed.")
    }

    private fun takeOrder(): List<String> {
        println("We are selling these items:")
        for ((name, item) in storeRoom.inventory) {
            print("   $name: ${item.count} @ ${currencyFormatter.format(item.price)} each")
            val special = specials.getOrDefault(name, null)
            special?.let {
                print(" (${special.itemsBought} for ${special.itemsCharged})")
            }
            println()
        }
        println("Please enter a space separated list of goods to order from this list:")

        // ensure no null values
        val orderLine = readLine() ?: ""
        if (orderLine.isBlank()) {
            return emptyList()
        }
        return orderLine.split(' ')
    }

    private fun processOrder(order: List<String>) {
        val (total, msg) = parseOrder(order)

        scope.launch {
            if (total < 0.0) {
                sendEvent("FAILED", total, msg)
            }
            else {
                println("Your total comes to ${currencyFormatter.format(total)}.")
                sendEvent("SUCCESS", total, msg)
            }
        }
    }

    /**
     * Parse the order for valid items and calculate costs, including special offers.
     *
     * If any items are invalid, reject order.
     */
    fun parseOrder(order: List<String>): Pair<Double, String> {
        val itemOrderCounts: MutableMap<String, Int> = mutableMapOf()
        var total = 0.0

        // count amount of each item purchased
        order.forEach {
            val count = itemOrderCounts.getOrDefault(it, 0) + 1
            itemOrderCounts[it] = count
        }
        // apply discounts where possible, then add any items at full price that were not on special or that weren't enough for a given special
        for ((name, orderCount) in itemOrderCounts) {
            // invalid items will void entire order
            val item = storeRoom.getItem(name) ?: return Pair(-1.0, "Invalid item ordered: [$name].")
            // remove items from inventory before calculating special offers
            if (!storeRoom.takeItems(name, orderCount)) {
                // not enough stock will also void entire order
                return Pair(-1.0, "Item [$name], $orderCount were ordered, but only ${item.count} in stock.")
            }

            val special = specials.getOrDefault(name, null)
            if (special == null) {
                total += item.price * orderCount
            }
            else {
                // how many full sets purchased at discount
                val disCount = orderCount / special.itemsBought
                total += disCount * special.itemsCharged * item.price

                // how many left over at full price
                val fullCount = orderCount % special.itemsBought
                total += fullCount * item.price
            }
        }
        return Pair(total, "OK")
    }

    private suspend fun sendEvent(type: String, total: Double, msg: String?) {
        if (channel.isClosedForSend) {
            return
        }
        val cm = ChannelMessage(type, total, msg)
        channel.send(cm)
    }
}

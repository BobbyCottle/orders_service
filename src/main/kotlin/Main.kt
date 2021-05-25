import java.text.NumberFormat

fun main(args: Array<String>) {
    var main:Main = Main()
    main.start()

//        while (true) {
    main.takeOrder()
//            break
//        }
}

class Main {
    var goods: MutableMap<String, Double> = mutableMapOf()
    val format: NumberFormat = NumberFormat.getCurrencyInstance()

    fun start(): Unit {
        println("Welcome to the Cottlin Orders System!")
        goods["apple"] = 0.60
        goods["orange"] = 0.25
    }

    fun takeOrder (): Unit {
        println("We are selling these items:")
        for ((item, price) in goods) {
            println("   $item: ${format.format(price)}")
        }
        println("Please enter a space separated list of goods to order from this list:")
        val order = readLine()!!.split(' ').toMutableList()
        val total = parseOrder(order)

        println("Your total comes to ${format.format(total)}.")
    }

    fun parseOrder(order: MutableList<String>): Double {
        val total: Double = order.fold(0.0) { total, item ->
            var itemPrice = goods.getOrDefault(item, 0.0)
            if (itemPrice == 0.0) {
                println("Sorry, we do not have '$item' for sale.")
            }
            total + itemPrice
        }
        return total
    }
}


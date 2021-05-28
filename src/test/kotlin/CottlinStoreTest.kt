import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class CottlinStoreTest {
    private val channel = Channel<ChannelMessage>();
    private var storeRoom: StoreRoom = StoreRoom()
    private var store: CottlinStore = CottlinStore(storeRoom, channel)

    @BeforeEach
    fun setUp() {
        storeRoom.inventory["apple"] = Item("apple", 0.60, 10)
        storeRoom.inventory["orange"] = Item("orange", 0.25, 8)
    }


    @Test
    fun testParseOrder() {
        // TODO: figure out how to turn off specials for this test

        var invalidItemList1 = mutableListOf("orange", "banana", "apple")
        assertEquals(Pair(-1.0, "Invalid item ordered: [banana]."), store.parseOrder(invalidItemList1))

        var invalidItemList2 = mutableListOf("apple", "")
        assertEquals(Pair(-1.0, "Invalid item ordered: []."), store.parseOrder(invalidItemList2))

        var item1List = mutableListOf("orange", "orange", "apple")
        assertEquals(Pair(0.5 + 0.6, "OK"), store.parseOrder(item1List))
    }

    @Test
    fun testParseOrderWithSpecials() {
        // 3 oranges, 4 apples
        var item1List = mutableListOf("orange", "orange", "orange", "apple", "apple", "apple", "apple")
        assertEquals(Pair(0.5 + 1.2, "OK"), store.parseOrder(item1List))

        // 5 oranges, 3 apples
        var item2List = mutableListOf("orange", "orange", "orange", "orange", "orange", "apple", "apple", "apple")
        assertEquals(Pair(1.0 + 1.2, "OK"), store.parseOrder(item2List))
    }
}
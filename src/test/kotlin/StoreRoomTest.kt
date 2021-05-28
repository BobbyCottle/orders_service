import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class StoreRoomTest {
    private lateinit var storeRoom: StoreRoom

    @BeforeEach
    fun setUp() {
        storeRoom = StoreRoom()
    }

    @Test
    fun getItem() {
        var item = storeRoom.getItem("apple")
        assertEquals(Item("apple", 0.60, 10), item)
    }

    @Test
    fun takeItems() {
        // unknown item
        assertFalse(storeRoom.takeItems("unknown", 10))
        // take all in stock
        assertTrue(storeRoom.takeItems("apple", 10))
        // try to take more
        assertFalse(storeRoom.takeItems("apple", 10))
    }
}
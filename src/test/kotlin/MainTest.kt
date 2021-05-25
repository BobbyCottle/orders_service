import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class MainTest {
    var main: Main = Main()

    @BeforeEach
    fun setUp() {
        main.start()
    }

    @AfterEach
    fun tearDown() {
    }

    //    @Test
    fun testTakeOrder() {
    }

    @Test
    fun testParseOrder() {
        var item1List = mutableListOf("banana", "orange", "apple", "apple", "apple")
        var item2List = mutableListOf("apple", "")
        assertEquals(2.05, main.parseOrder(item1List))
        assertEquals(0.6, main.parseOrder(item2List))
    }
}

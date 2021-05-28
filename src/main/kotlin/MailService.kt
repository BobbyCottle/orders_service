import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.runBlocking
import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime


class MailService(_channel: ReceiveChannel<ChannelMessage>) {
    private val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance()
    private val channel = _channel

    fun start() {
        println("Mail: starting monitoring of published order events...")
        while (!channel.isClosedForReceive) {
            // break on null message
            val evtMsg = receiveEvent() ?: break
            notifyCustomer(evtMsg)
        }
        println("Mail: event channel is closed, Mail Service closing.")
    }

    private fun receiveEvent(): ChannelMessage? = runBlocking {
        return@runBlocking try {
            channel.receive()
        }
        catch (e: Exception) {
            null
        }
    }

    private fun notifyCustomer(message: ChannelMessage) {
        val notice: String
        if (message.type == "SUCCESS") {
            // assume 5 minutes delivery time
            val expireTime = LocalDateTime.parse(message.time).plusMinutes(5)
            val remaining = Duration.between(LocalTime.now(), expireTime)
            notice = if (remaining.seconds > 0) {
                val mm = remaining.toMinutes()
                val ss = remaining.minusMinutes(mm).seconds
                "Order completed successfully. Total = ${numberFormat.format(message.total)}. You may expect delivery in $mm minutes and $ss seconds."
            }
            else {
                "Order completed successfully. Total = ${numberFormat.format(message.total)}. Your items have been delivered."
            }
        }
        else {
            notice = "problem processing your order:\n   ${message.msg}\n   Please try again."
        }
        println("Mail: Order Status - $notice")
    }
}

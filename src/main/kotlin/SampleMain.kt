import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    println("MAIN: Starting application...")
    val scope = CoroutineScope(Job())
    val channel = Channel<SampleChannelMessage>(Channel.UNLIMITED)
    channel.invokeOnClose {
        println("MAIN: Channel closed reason: ${it?.message}")
        scope.coroutineContext.cancelChildren()
    }

    val pubJob = scope.launch {
        PubApp(channel).start()
    }
    val subJob = scope.launch {
        SubApp(channel).start()
    }
    // keep the whole program going until user cancels
    runBlocking {
        pubJob.join()
        subJob.join()
    }
    println("MAIN: Shut down application.")
}

data class SampleChannelMessage(val type: String, val total: Double, val msg: String?) {
    val time: String = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
}

class PubApp(_channel: SendChannel<SampleChannelMessage>) {
    private val scope = CoroutineScope(Job())
    private val channel = _channel
    fun start() {
        println("PubApp: starting shop...")
        while (!channel.isClosedForSend) {
            val msg = getMessage()
            if (msg == "") {
                println("PubApp: closing channel...")
                channel.close()
                println("PubApp: channel closed.")
                break
            }
            println("PubApp: You entered: [$msg]")
            scope.launch {
                sendEvent(msg)
            }
        }

        println("PubApp: Customers tired, closing shop.")
    }

    private fun getMessage(): String {
        println("PubApp: Please enter a message: ")
        return readLine() ?: ""
    }

    private suspend fun sendEvent(msg: String) {
        val cm = SampleChannelMessage("SUCCESS", 1.0, msg)
        println("PubApp: sending message...")
        channel.send(cm)
        println("PubApp: message [$msg] sent.")
    }
}

class SubApp(_channel: ReceiveChannel<SampleChannelMessage>) {
    private val scope = CoroutineScope(Job())
    private val channel = _channel

    fun start() {
        println("SubApp: starting monitoring of published events...")
        while (!channel.isClosedForReceive) {
            val evtMsg = receiveEvent() ?: break
            notifyCustomer(evtMsg)
        }
        println("SubApp: event channel is closed, closing event monitoring.")
    }

    class SourceClosed(cause: Throwable) : IllegalStateException(cause)

    private fun receiveEvent(): SampleChannelMessage? = runBlocking {
        println("SubApp: getting published event...")
        var cm: SampleChannelMessage?
        try {
            println("SubApp: receiving message...")
            cm = channel.receive()
            println("SubApp: message [$cm] received.")
        }
        catch (e: ClosedReceiveChannelException) {
            // channel closed, time to shut down
            // throw SourceClosed(e)
            cm = null
            println("SubApp: channel closed, time to shut down.")
        }
        cm
    }

    private fun notifyCustomer(evtMsg: SampleChannelMessage?) {
        println("SubApp: received published event:\n   $evtMsg")
    }
}

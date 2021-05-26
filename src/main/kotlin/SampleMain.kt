import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun main(args: Array<String>) {
    val channel = Channel<SampleChannelMessage>(Channel.UNLIMITED);
    val scope = CoroutineScope(Job())
    scope.launch {
        SubApp(channel).start()
    }
    scope.launch {
        PubApp(channel).start()
    }

//    runBlocking {
//        launch {
//            SubApp(channel).start()
//        }
//        launch {
//            PubApp(channel).start()
//        }
//    }
}

data class SampleChannelMessage(val type: String, val total: Double, val msg: String?) {
    val time: String = LocalTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
}

class PubApp(_channel: SendChannel<SampleChannelMessage>) {
    private val scope = CoroutineScope(Job())
    private val channel = _channel
    fun start() {
        println("PubApp: getting and publishing events...")
        while (getMessage()) {
        }
        channel.close()
    }

    private fun getMessage(): Boolean {
        print("Please enter a message: ")
        val msg = readLine()!!
        println("")
        sendEvent(msg)
        return true
    }

    private fun sendEvent(msg: String) {
        scope.launch {
            val cm = SampleChannelMessage("SUCCESS", 1.0, msg)
            channel.send(cm)
            println("PubApp: published event.")
            channel.close()
        }
    }
}

class SubApp(_channel: ReceiveChannel<SampleChannelMessage>) {
    private val scope = CoroutineScope(Job())
    private val channel = _channel
    fun start() {
        println("SubApp: monitoring published events...")
        receiveEvent()
    }

    private fun receiveEvent() {
        var keepReading = true
        while (keepReading) {
            try {
                scope.launch {
                    var cm: SampleChannelMessage = channel.receive()
                    println("SubApp: received published event:")
                    println(cm)
                }
            }
            catch (e: Exception) {
                keepReading = false
            }
        }
    }
}

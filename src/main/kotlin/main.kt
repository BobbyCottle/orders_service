import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ChannelMessage(val type: String, val total: Double, val msg: String?) {
    val time: String = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
}
fun main(args: Array<String>) {
    println("MAIN: Starting application...")
    val storeRoom = StoreRoom()
    val scope = CoroutineScope(Job())
    val channel = Channel<ChannelMessage>(Channel.UNLIMITED);
    channel.invokeOnClose {
        // println("MAIN: Channel closed reason: ${it?.message}")
        scope.coroutineContext.cancelChildren()
    }
    val storeJob = scope.launch {
        CottlinStore(storeRoom, channel).start()
    }
    val mailJob = scope.launch {
        MailService(channel).start()
    }

    // keep the whole program going until user cancels
    runBlocking {
        storeJob.join()
        mailJob.join()
    }
    println("MAIN: Shut down application.")
}

package websocket
import java.time.LocalDateTime

object WebSocketDebugger {
    var enabled = false
    var output = true

    private val messages = mutableListOf<String>()
    fun log(message: String){
        if(enabled){
            val _message = "${LocalDateTime.now()} : " + message
            messages.add(_message)

            if(output) println(_message)
        }
    }

    fun assert(bool: Boolean, message: String){
        if(bool) log("[assert] : $message")
    }

    fun printAll(){
        for(message in messages){
            println(message)
        }
    }
}
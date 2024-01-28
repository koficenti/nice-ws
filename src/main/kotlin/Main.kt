import websocket.*

fun main(){
    WebSocketDebugger.enabled = true
    WebSocketDebugger.output = true

    val server: WebSocketServer = WebSocketServer(1234)

    val messages = hashMapOf("say hi" to "Hello!")

    server.onMessageReceived { agent, input ->
        if (messages.containsKey(input)){
            WebSocketDebugger.log("Got '$input' so sending '${messages[input]}'")
            WebSocketMessage.sendMessage(agent, messages[input]!!)
        }
    }

    server.onBinaryReceived { agent, bytes ->
        if(bytes.isNotEmpty() && 2.toByte() == bytes[0]){
            WebSocketDebugger.log("starts with 2")
        }
    }

    server.start()

}
import websocket.*
import websocket.WebSocketFeatures.ChannelStore

fun main(){
    WebSocketDebugger.enabled = true
    WebSocketDebugger.output = true

    val server = WebSocketServerBuilder(12345)
        .withChannels(allowedChannels = mutableListOf("default", "chat", ""))
        .build()

    server.onMessageReceived { agent, message ->
        val query = ChannelStore.parse(message)

        if(query != null){
            when(query.first){
                "" -> {
                    ChannelStore.broadcast("default", "${agent.id} : ${query.second}")
                }
                "default" -> {
                    ChannelStore.broadcast("default", "${agent.id} : ${query.second}")
                }
                "chat" -> {
                    ChannelStore.broadcast("chat", "${agent.id} : ${query.second}")
                }
            }
        }
    }

    server.onConnectionOpened { agent ->
        ChannelStore.addToChannel("default", agent.id.toString())
        ChannelStore.broadcast("default", "${agent.id} : has Joined! 👍")
    }

    server.onConnectionClosed { agent ->
        ChannelStore.broadcast("default", "${agent.id} : has Left! 💀")
    }

    server.start()
}
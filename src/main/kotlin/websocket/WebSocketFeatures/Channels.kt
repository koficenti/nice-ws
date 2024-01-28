package websocket.WebSocketFeatures

import websocket.*
import java.util.concurrent.ConcurrentHashMap

object ChannelStore {
    var feature_on = false
    val channels = ConcurrentHashMap<String, MutableSet<String>>()
    var allowedChannels: MutableList<String> = mutableListOf("")

    fun isChannel(channel: String, message: String): Boolean{
        return message.startsWith("$channel://")
    }
    fun parse(message: String): Pair<String, String>?{
        val parts = message.split("://")
        if(parts.size >= 2 && allowedChannels.filter { message.startsWith(it + "://")  }.isNotEmpty()){
            return Pair(parts[0], parts.drop(1).joinToString(""))
        }else if (channels.containsKey("")){
            return Pair("", message)
        }
        return null
    }
    fun broadcast(channel: String, message: String){
        if(channels.containsKey(channel)){
            for (id in channels[channel]!!){
                if(ClientStore.clients.containsKey(id)){
                    WebSocketDebugger.log("Sending $id .. $message")
                    WebSocketMessage.sendMessage(ClientStore.clients[id]!!, message)
                }
            }
        }else {
            WebSocketDebugger.log("Channel '$channel' broadcast failed!")
        }
    }

    fun addToChannel(channel: String, id: String){
        if(allowedChannels.contains(channel) && channels.containsKey(channel)){
            channels[channel]!!.add(id)
        }
    }
}
class Channels(
    server: WebSocketServer,
    private val autoCreate: Boolean = false
) : WebSocketServer(port = server._port) {

    init {
        ChannelStore.feature_on = true
        for(channel in ChannelStore.allowedChannels) {
            ChannelStore.channels[channel] = mutableSetOf()
        }
    }
    override fun onMessageReceived(handler: (WebSocketServerAgent, String) -> Unit) {
        messagedReceived = { agent, message ->
            for(channel in ChannelStore.allowedChannels){
                if(ChannelStore.isChannel(channel, message)){
                    if(ChannelStore.channels.containsKey(channel))
                        ChannelStore.channels[channel]?.add(agent.id.toString())
                    }
                }
                handler(agent, message)
            }
        }
    }
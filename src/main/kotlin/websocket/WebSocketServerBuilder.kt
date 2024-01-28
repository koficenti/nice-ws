package websocket

import websocket.WebSocketFeatures.ChannelStore
import websocket.WebSocketFeatures.Channels

class WebSocketServerBuilder(private val port: Int) {
    var result = object : WebSocketServer(port = port){}
    fun withLogging(): WebSocketServerBuilder{ return this}
    fun withAuth(): WebSocketServerBuilder{ return this}
    fun withBroadcasting(channel: String): WebSocketServerBuilder{ return this}

    fun withChannels(allowedChannels: MutableList<String>): WebSocketServerBuilder {
        ChannelStore.allowedChannels = allowedChannels
        result = Channels(result)
        return this
    }
    fun withFeature(feature: WebSocketServerFeatures): WebSocketServerBuilder {return this}

    fun build(): WebSocketServer{
        return result
    }
}
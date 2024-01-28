package websocket

class WebSocketServerBuilder(private val port: Int) {
    fun withLogging(): WebSocketServerBuilder{ return this}
    fun withAuth(): WebSocketServerBuilder{ return this}
    fun withBroadcasting(channel: String): WebSocketServerBuilder{ return this}

    fun withFeature(feature: WebSocketServerFeatures): WebSocketServerBuilder {return this}
    fun build(): WebSocketServer{
        return WebSocketServer(port = port)
    }
}
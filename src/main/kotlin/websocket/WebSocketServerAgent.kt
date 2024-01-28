package websocket

import java.net.Socket

data class WebSocketServerAgent(val name: String, val id: Int, val socket: Socket, var close: Boolean = false)
package websocket

import java.net.Socket
import java.util.UUID

data class WebSocketServerAgent(val id: UUID, val socket: Socket, var close: Boolean = false)
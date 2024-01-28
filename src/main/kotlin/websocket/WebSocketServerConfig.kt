package websocket

class WebSocketServerConfig(
    val connectionBehavior: WebSocketServerBehaviors?,
    val throttleBehavior: WebSocketServerBehaviors?,
)
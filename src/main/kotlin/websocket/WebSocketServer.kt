package websocket

import java.net.ServerSocket
import java.net.Socket
import kotlinx.coroutines.*
import java.net.SocketException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class WebSocketServer(val port: Int) {
    var config: WebSocketServerConfig = WebSocketServerConfig(null, null)
    private var serverSocket: ServerSocket? = null

    private val clients = ConcurrentHashMap<String, WebSocketServerAgent>()

    private suspend fun handleConnection(client: Socket) = withContext(Dispatchers.IO){

        val id = clientCounter.incrementAndGet()
        val agent = WebSocketServerAgent("User-$id", id, client)

        clients["User-$id"] = agent
        connectionOpened(agent)

        WebSocketDebugger.log("Connection Established")

        try {
            val input = client.getInputStream()
            val output = client.getOutputStream()

            while (!agent.close) {
                val frame = WebSocketMessage.readWebSocketFrame(input)

                    when (frame.Opcode) {
                        0x1 -> {
                            messagedReceived(agent, String(frame.payload, Charsets.UTF_8))
                        }
                        0x2 -> {
                            binaryReceived(agent, frame.payload)
                        }
                        0x8 -> {
                            connectionClosed(agent)
                            break
                        }

                        else -> continue
                    }
            }
        } finally {
            try{
                client.shutdownInput()
                client.shutdownOutput()
                client.close()
                WebSocketDebugger.log("Connection Closed Without Errors")
            } catch (error: Exception){
                WebSocketDebugger.log("Connection Closed With Errors")
            }
        }
    }


    fun start() {
        serverSocket = ServerSocket(port)

        while (serverSocket != null){
            val clientSocket = try {
                serverSocket!!.accept()
            } catch (_: SocketException){
                WebSocketDebugger.log("Server and clients closed safely! Goodbye!")
                return
            }

            val input = clientSocket.getInputStream()
            val output = clientSocket.getOutputStream()

            WebSocketMessage.performHandshake(input, output)

            runBlocking {
                launch(Dispatchers.IO) {
                    handleConnection(clientSocket)
                }
            }
        }
    }
    fun stop() {
        clients.forEach {
            it.value.close = true
            clients.remove(it.key)
        }

        if (serverSocket != null) serverSocket!!.close()

    }

    private var messagedReceived: (WebSocketServerAgent, String) -> Unit = { webSocketServerAgent: WebSocketServerAgent, s: String -> };
    private var binaryReceived: (WebSocketServerAgent, ByteArray) -> Unit = { webSocketServerAgent: WebSocketServerAgent, b: ByteArray -> };

    private var connectionOpened: (WebSocketServerAgent) -> Unit = {};
    private var connectionClosed: (WebSocketServerAgent) -> Unit = {};

    fun onMessageReceived(handler: (WebSocketServerAgent, String) -> Unit){
        messagedReceived = handler
    }
    fun onBinaryReceived(handler: (WebSocketServerAgent, ByteArray) -> Unit){
        binaryReceived = handler
    }
    fun onConnectionOpened(handler: (WebSocketServerAgent) -> Unit){
        connectionOpened = handler
    }
    fun onConnectionClosed(handler: (WebSocketServerAgent) -> Unit){
        connectionClosed = handler
    }
    fun onError(handler: (WebSocketServerAgent) -> Unit){

    }

    fun broadcast(channel: String, message: String){}

    companion object {
        private val clientCounter = AtomicInteger()
    }
}
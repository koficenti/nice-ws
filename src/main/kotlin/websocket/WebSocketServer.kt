package websocket

import com.sun.security.ntlm.Client
import java.net.ServerSocket
import java.net.Socket
import kotlinx.coroutines.*
import java.net.SocketException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.exitProcess

object ClientStore{
    val clients = ConcurrentHashMap<String, WebSocketServerAgent>()
}
abstract class WebSocketServer(port: Int) {
    val _port = port
    open var config: WebSocketServerConfig = WebSocketServerConfig(null)
    private var serverSocket: ServerSocket? = null

    var messagedReceived = {_: WebSocketServerAgent, _: String -> Unit}
    var binaryReceived = {_: WebSocketServerAgent, _: ByteArray -> Unit}
    var connectionOpened = {_: WebSocketServerAgent -> Unit}
    var connectionClosed = {_: WebSocketServerAgent -> Unit}

    open fun onMessageReceived(handler: (WebSocketServerAgent, String) -> Unit){
        messagedReceived = handler
    }
    open fun onBinaryReceived(handler: (WebSocketServerAgent, ByteArray) -> Unit){
        binaryReceived = handler
    }
    open fun onConnectionOpened(handler: (WebSocketServerAgent) -> Unit){
        connectionOpened = handler
    }
    open fun onConnectionClosed(handler: (WebSocketServerAgent) -> Unit){
        connectionClosed = handler
    }
    open fun onError(handler: (WebSocketServerAgent) -> Unit){

    }

    open fun send(id: String, message: String){
        if(ClientStore.clients.containsKey(id)){
            WebSocketMessage.sendMessage(ClientStore.clients[id]!!, message)
        }
    }

    open fun start() = runBlocking {
        serverSocket = ServerSocket(_port)

        while (serverSocket != null){
            val clientSocket = try {
                serverSocket!!.accept()
            } catch (_: SocketException){
                WebSocketDebugger.log("Server and clients closed safely! Goodbye!")
                exitProcess(1)
            }

            val input = clientSocket.getInputStream()
            val output = clientSocket.getOutputStream()

            WebSocketMessage.performHandshake(input, output)

            launch(Dispatchers.IO) {
                handleConnection(clientSocket)
            }
        }
    }
    open fun stop() {
        ClientStore.clients.forEach {
            it.value.close = true
            ClientStore.clients.remove(it.key)
        }

        if (serverSocket != null) serverSocket!!.close()

    }

    open suspend fun handleConnection(client: Socket) = withContext(Dispatchers.IO){

        val id = UUID.randomUUID()
        val agent = WebSocketServerAgent(id, client)

        ClientStore.clients[id.toString()] = agent
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
                WebSocketDebugger.log("Connection Was Not Closed Properly?")
            }
        }

        if(ClientStore.clients.containsKey(id.toString())){
            ClientStore.clients.remove(id.toString())
        }
    }
}
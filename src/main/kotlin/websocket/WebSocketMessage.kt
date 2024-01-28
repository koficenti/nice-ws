package websocket

import java.io.*
import java.net.Socket
import java.security.MessageDigest
import java.util.*


/*
*
* Helper object for reading and creating websocket messages/frames
*
*
*
* */

object WebSocketMessage {
    data class WebSocketFrame(
        val FIN: Boolean,
        val RSV1: Boolean,
        val RSV2: Boolean,
        val RSV3: Boolean,
        val Opcode: Int,
        val mask: Boolean,
        val length: Int,
        val maskingKey: ByteArray?,
        val payload: ByteArray
    )

    fun readWebSocketFrame(inputStream: InputStream): WebSocketFrame {
        val message = ByteArray(2)
        inputStream.read(message, 0, 2)

        val FIN = (message[0].toInt() and 0x80) != 0
        val RSV1 = (message[0].toInt() and 0x40) != 0
        val RSV2 = (message[0].toInt() and 0x20) != 0
        val RSV3 = (message[0].toInt() and 0x10) != 0
        val Opcode = message[0].toInt() and 0x0F
        val mask = (message[1].toInt() and 0x80) != 0
        var length = (message[1].toInt() and 0x7F)

        var nextByte = 2
        if (length == 126) {
            // length = next 2 bytes
            val lengthBytes = ByteArray(2)
            inputStream.read(lengthBytes, 0, 2)
            length = ((lengthBytes[0].toInt() and 0xFF) shl 8) or (lengthBytes[1].toInt() and 0xFF)
            nextByte += 2
        } else if (length == 127) {
            // length = next 8 bytes
            // Note: This assumes the length is within the range of Int in Kotlin
            val lengthBytes = ByteArray(8)
            inputStream.read(lengthBytes, 0, 8)
            length = 0
            for (i in 0 until 8) {
                length = (length shl 8) or (lengthBytes[i].toInt() and 0xFF)
            }
            nextByte += 8
        }

        var maskingKey: ByteArray? = null
        if (mask) {
            maskingKey = ByteArray(4)
            inputStream.read(maskingKey, 0, 4)
            nextByte += 4
        }

        val payload = ByteArray(length)
        inputStream.read(payload, 0, length)

        if (maskingKey != null) {
            for (i in payload.indices) {
                payload[i] = (payload[i].toInt() xor maskingKey[i % 4].toInt()).toByte()
            }
        }

        return WebSocketFrame(FIN, RSV1, RSV2, RSV3, Opcode, mask, length, maskingKey, payload)
    }

    fun performHandshake(input: InputStream, output: OutputStream) {
        val reader = BufferedReader(InputStreamReader(input))
        val writer = PrintWriter(output, true)

        var key: String? = null

        // Read and find the Sec-WebSocket-Key header
        while (true) {
            val line = reader.readLine() ?: break
            if (line.startsWith("Sec-WebSocket-Key:")) {
                key = line.substringAfter(":").trim()
                break
            }
        }

        if (key != null) {
            // Concatenate the key with the WebSocket GUID and calculate SHA-1 hash
            val concatenated = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
            val sha1 = MessageDigest.getInstance("SHA-1").digest(concatenated.toByteArray())
            val responseKey = Base64.getEncoder().encodeToString(sha1)

            // Send the WebSocket handshake response
            writer.println("HTTP/1.1 101 Switching Protocols")
            writer.println("Upgrade: websocket")
            writer.println("Connection: Upgrade")
            writer.println("Sec-WebSocket-Accept: $responseKey")
            writer.println() // Empty line to indicate the end of headers
            writer.flush()
        } else {
            // Handle the case where the Sec-WebSocket-Key header is not found
            WebSocketDebugger.log("Sec-WebSocket-Key not found in handshake request.")
        }
    }
    fun createWebSocketFrame(
            payload: ByteArray,
            fin: Boolean = true,
            RSV1: Boolean = false,
            RSV2: Boolean = false,
            RSV3: Boolean = false,
            opcode: Int = 1,
            mask: Boolean = false,
            length: Int = payload.size,
            maskingKey: ByteArray? = null,
        ): ByteArray {

        val finBit = if (fin) 0x80 else 0x00
        val opcodeAndMask = opcode or finBit

        val frameHeader = byteArrayOf(
            opcodeAndMask.toByte(),
            length.toByte()
        )

        return frameHeader + payload
    }

    fun sendMessage(agent: WebSocketServerAgent, message: String){
        try {
            agent.socket.getOutputStream().write(createWebSocketFrame(message.toByteArray(), opcode = 1))
            agent.socket.getOutputStream().flush()
        } catch (e: Error){
            WebSocketDebugger.log("Error -> .sendMessage() failed to write to output stream!")
        }
    }
    fun sendBinary(agent: WebSocketServerAgent, binary: ByteArray){
        try {
            agent.socket.getOutputStream().write(createWebSocketFrame(binary, opcode = 2))
            agent.socket.getOutputStream().flush()
        } catch (e: Error){
            WebSocketDebugger.log("Error -> .sendBinary() failed to write to output stream!")
        }
    }
}
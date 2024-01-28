
## A Reasonably Nice Web Socket Library


#### What is 'nice-ws':

Nice-ws is a web socket library written kotlin. \
Currently with only server support. \
The goal for this project is to become very feature rich and easy to use for the general use cases.
######  This library is new and not for production, you were warned ⚠️

\
``` Expect breaking changes! ```
#### Simple usage:
Creates a simple web socket server that if it receives the message "say hi" it will send the corresponding message to the client, "Hello"
```kotlin
fun main(){
    WebSocketDebugger.enabled = true
    WebSocketDebugger.output = true

    val server: WebSocketServer = WebSocketServer(1234)

    val messages = hashMapOf("say hi" to "Hello!")

    server.onMessageReceived { agent, input ->
        if (messages.containsKey(input)){
            WebSocketDebugger.log("Got '$input' so sending '${messages[input]}'")
            WebSocketMessage.sendMessage(agent, messages[input]!!)
        }
    }

    server.onBinaryReceived { agent, bytes ->
        if(bytes.isNotEmpty() && 0.toByte() == bytes[0]){
            WebSocketDebugger.log("starts with 0")
        }
    }
    
    server.onConnectionOpened { agent -> null } // do something
    server.onConnectionClosed { agent -> null } // do something
    
    server.start()
}

```

### Currently working on:
- Auth / Validation
- Advanced Logging
- Throttling
- Rate Limiting
- Compression
- Encryption
- Monitoring
- Broadcasting
- Auditing
- CustomHeader
- Message Filtering


### Use cases:
(You can do a lot of things)
- Chat app
- Live dashboards
- Game server
- File sharing
- Live voting features
- Automated Systems (Home automation)
- Real time tracking and monitoring
- Collaboration Oriented Projects
- Log streaming
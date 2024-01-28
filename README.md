
## A Reasonably Nice Web Socket Library


#### What is 'nice-ws':

Nice-ws is a web socket library written in Kotlin (the programming language). \
Currently with server support only (client coming soon 👍). \
The goal for this project is to become very feature rich and easy to use for the general use cases.
######  This library is new and not for production, you were warned ⚠️


#### Why was 'nice-ws' created?

A few reasons:
- Currently looking for a job. Hoping this makes me stand out a tiny bit more.
- And I plan to make a web framework that requires no javascript knowledge to use, by using web sockets to update the UI (inspired by Phoenix the elixir framework).



\
``` Expect breaking changes! ```
#### Simple usage:
Creates a simple web socket server that if it receives the message "say hi" it will send the corresponding message to the client, "Hello"
```kotlin
// this code works for the moment 🫢

fun main(){
    WebSocketDebugger.enabled = true
    WebSocketDebugger.output = true

    val server = WebSocketServerBuilder(12345)
        .withChannels(allowedChannels = mutableListOf("default", "chat", ""))
        .build()

    server.onMessageReceived { agent, message ->
        val query = ChannelStore.parse(message)

        if(query != null){
            when(query.first){
                "" -> {
                    ChannelStore.broadcast("default", "${agent.id} : ${query.second}")
                }
                "default" -> {
                    ChannelStore.broadcast("default", "${agent.id} : ${query.second}")
                }
                "chat" -> {
                    ChannelStore.broadcast("chat", "${agent.id} : ${query.second}")
                }
            }
        }
    }

    server.onConnectionOpened { agent ->
        ChannelStore.addToChannel("default", agent.id.toString())
        ChannelStore.broadcast("default", "${agent.id} : has Joined! 👍")
    }

    server.onConnectionClosed { agent ->
        ChannelStore.broadcast("default", "${agent.id} : has Left! 💀")
    }

    server.start()
}

```

### Currently working on:
- Channels (or rooms)
- Auth / Validation (or Permissions)
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
- Task Factory
- Probably more ideas later on 😁


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

### Benchmarks & Comparisons:

```
Coming soon!
```


### About me

```json
{
  "currently_looking_for_job": true,

  "name": "Joshua Hunter",
  "title": "Full Stack Developer",
  
  "email": "coming soon",
  "favorites_languages": "Kotlin 🥰, Haskell 🔥, C++ 👍 "
}
```
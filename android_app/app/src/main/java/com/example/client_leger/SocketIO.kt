package com.example.client_leger

import android.util.Log
import com.example.client_leger.Communication.Communication
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketIO {
    private var socket = IO.socket(Constants.SERVER_URL)

    init {
        socket.on(Socket.EVENT_CONNECT) {
            Log.w("socket","Connected to ${Constants.SERVER_URL}")
        }

        socket.on("chat") {
            Communication.updateChatMessage(it[0] as JSONObject)
        }
    }

    fun connect(username: String) {
        socket.connect()
        socket.emit("login", username)
    }

    fun disconnect() {
        socket.emit("logout")
    }

    fun sendMessage(event: String, obj: JSONObject){
        socket.emit(event, obj)
    }

}
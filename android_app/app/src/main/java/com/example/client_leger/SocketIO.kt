package com.example.client_leger

import android.util.Log
import com.example.client_leger.Communication.Communication
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject

object SocketIO {
    private var socket = IO.socket(Constants.SERVER_URL)

    fun init() {
        socket.connect()
        socket.on(Socket.EVENT_CONNECT) {
            Log.w("socket","Connected to ${Constants.SERVER_URL}")
        }

        socket.on("chat")               { Communication.updateChatMessage(it[0] as JSONObject) }
        socket.on("logging")            { Communication.updateConnection(it[0] as JSONObject) }
        socket.on("draw")               { Communication.updateDraw(it[0] as JSONObject) }

        socket.on("channel-new")        { Communication.channelAdded((it[0] as JSONObject)["id"].toString()) }
        socket.on("channel-delete")     { Communication.channelRemoved((it[0] as JSONObject)["channel"].toString()) }

        socket.on("game-start")         { Communication.updateGameStart() }
        socket.on("game-clear")         { Communication.updateGameClear() }
        socket.on("game-over")          { Communication.updateEndGame(it[0] as JSONObject) }
        socket.on("game-timer")         { Communication.updateTimer(it[0] as JSONObject) }
        socket.on("game-drawer")        { Communication.updateDrawer(it[0] as JSONObject) }
        socket.on("game-chat")          { Communication.updateGameChat(it[0] as JSONObject) }
        socket.on("game-points")        { Communication.updateGamePoints(it[0] as JSONObject) }
        socket.on("game-guessLeft")     { Communication.updateGuessLeft(it[0] as JSONObject) }

        socket.on("lobby-chat")         { Communication.updateLobbyChat(it[0] as JSONObject) }
        socket.on("lobby-notif")        { Communication.updateLobby(it[0] as JSONObject) }
        socket.on("lobby-kicked")       { Communication.updateKick() }
        socket.on("lobby-invitation")   { Communication.updateInvitation(it[0] as JSONObject) }
    }

    fun connect(username: String) {
        socket.emit("login", username)
    }

    fun disconnect() {
        socket.emit("logout")
    }

    fun sendMessage(event: String, obj: JSONObject){
        socket.emit(event, obj)
    }
}
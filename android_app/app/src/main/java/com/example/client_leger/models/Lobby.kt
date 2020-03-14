package com.example.client_leger.models

import android.util.Log
import org.json.JSONObject


class Lobby() {
    var usernames = arrayListOf<String>()
    var private = false
    var size = 0
    var lobbyName = ""
    var gameID = ""

    constructor(jsonObject: JSONObject) : this() {
        private = jsonObject.getBoolean("private")
        size = jsonObject.getInt("size")
        lobbyName = jsonObject.getString("lobbyName")
        gameID = jsonObject.getString("gameID")
    }
}
package com.example.client_leger.Models

import android.app.Activity
import android.content.Context
import android.widget.ArrayAdapter
import com.example.client_leger.Fragments.GameCardsFragment
import org.json.JSONObject

class GameCard constructor(context: Context) {
    var gameName = ""
    var gameId = ""
    var gameMode = ""
    var lobbies = arrayListOf<Lobby>()
    var adapter =  ArrayAdapter<String>(
        context,
        android.R.layout.simple_list_item_1
        )
    constructor(jsonObject: JSONObject, context: Context) : this(context= context) {
        gameName = jsonObject.getString("gameName")
        gameId = jsonObject.getString("gameID")
        gameMode = jsonObject.getString("mode")
    }

}
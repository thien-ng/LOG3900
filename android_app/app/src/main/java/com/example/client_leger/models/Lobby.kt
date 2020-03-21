package com.example.client_leger.models

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import org.json.JSONArray
import org.json.JSONObject


class Lobby(jsonObject: JSONObject, context: Context) {
    var usernames = jsonArrayToStringArray(jsonObject.getJSONArray("usernames"))

    var private: Boolean = jsonObject.getBoolean("private")
    var size: Int = jsonObject.getInt("size")
    var lobbyName: String = jsonObject.getString("lobbyName")
    var gameMode: String = jsonObject.getString("mode")
    var adapter = ArrayAdapter<String>(
        context,
        R.layout.simple_list_item_1,
        usernames
    )
    private fun jsonArrayToStringArray(usersJsonArray: JSONArray): ArrayList<String> {
        var tempArray: ArrayList<String> = arrayListOf()
        for (i in 0 until usersJsonArray.length())
            tempArray.add(usersJsonArray.get(i) as String)
        return tempArray
    }

}
package com.example.client_leger.models

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

class Lobby(jsonObject: JSONObject, context: Context) : Serializable{
    var lobbyName: String = jsonObject.getString("lobbyName")
    var usernames: ArrayList<String>
    var private: Boolean
    var size: Int
    var gameMode: String;
    var adapter: ArrayAdapter<String>

    init {
        usernames = if (jsonObject.isNull("usernames")) arrayListOf() else jsonArrayToStringArray(jsonObject.getJSONArray("usernames"))
        private = if (jsonObject.isNull("isPrivate")) false else jsonObject.getBoolean("isPrivate")
        size = if (jsonObject.isNull("size")) 0 else jsonObject.getInt("size")
        gameMode = if (jsonObject.isNull("mode")) "FFA" else jsonObject.getString("mode")

        adapter = ArrayAdapter<String>(context, R.layout.simple_list_item_1, usernames)
    }

    private fun jsonArrayToStringArray(usersJsonArray: JSONArray): ArrayList<String> {
        var tempArray: ArrayList<String> = arrayListOf()
        for (i in 0 until usersJsonArray.length())
            tempArray.add(usersJsonArray.get(i) as String)
        return tempArray
    }

}
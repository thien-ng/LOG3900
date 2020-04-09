package com.example.client_leger.models

import org.json.JSONArray
import org.json.JSONObject

class User(username:String, fName:String, lName:String, connections: JSONArray, stats:JSONObject, games: JSONArray) {
    var name = username
    var fName = fName
    var lName = lName

    var connections = connections

    var stats:Stats = Stats(stats.getInt("totalGame"), stats.getDouble("winRate"), stats.getInt("bestScore"), stats.getInt("totalPlayTime") , stats.getDouble("avgGameTime"))
    var games:ArrayList<Game> = jsonArrayToGameArray(games)

    private fun jsonArrayToGameArray(games: JSONArray): ArrayList<Game> {
        var tempArray: ArrayList<Game> = arrayListOf()
        for (i in 0 until games.length()) {
            var jsonGame = games.getJSONObject(i)
            var tempGame = Game(jsonGame.getString("mode"),jsonGame.getString("date"), jsonGame.getJSONArray("players").toString())
            tempArray.add(tempGame)
        }
        return tempArray
    }

}
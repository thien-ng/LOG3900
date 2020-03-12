package com.example.client_leger.Controller

import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.Constants
import com.example.client_leger.Fragments.GameCardsFragment
import com.example.client_leger.Models.GameCard
import com.example.client_leger.Models.Lobby
import org.json.JSONArray

class GameCardsController {
    fun getGameCards(activity: GameCardsFragment) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonObjectRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.CARD_ENDPOINT,
            null,
            Response.Listener<JSONArray> { response ->
                if (response.length() > 0) activity.loadGameCards(response)
            }, Response.ErrorListener { error ->
                Toast.makeText(activity.context, error.message, Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    fun getLobbies(activity: GameCardsFragment, gameID:String){
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonObjectRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.ACTIVE_LOBBY_ENDPOINT + "/" + gameID,
            null,
            Response.Listener<JSONArray> { response ->
                if (response.length() > 0) activity.loadLobbies(responseToLobbies(response), gameID)
            }, Response.ErrorListener { error ->
                Toast.makeText(activity.context, error.message, Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun responseToLobbies(response: JSONArray): ArrayList<Lobby>{
        var lobbies = arrayListOf<Lobby>()
        for(i in 0 until response.length()){
            lobbies.add(Lobby(response.getJSONObject(i)))
        }
        return lobbies
    }
}
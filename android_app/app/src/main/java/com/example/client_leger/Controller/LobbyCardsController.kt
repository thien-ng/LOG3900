package com.example.client_leger.Controller

import android.content.Context
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.Constants
import com.example.client_leger.Fragments.LobbyCardsFragment
import com.example.client_leger.models.Lobby
import org.json.JSONArray
import org.json.JSONObject

class LobbyCardsController {

    fun joinLobby(activity: LobbyCardsFragment, body: JSONObject){
        val mRequestQueue = Volley.newRequestQueue(activity.context)

        val mJsonObjectRequest = object : StringRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.LOBBY_JOIN_ENDPOINT,
            Response.Listener {

            },
            Response.ErrorListener {error->
                Toast.makeText(activity.context, error.toString(), Toast.LENGTH_SHORT).show()
            }) {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                return body.toString().toByteArray()
            }
        }
        mRequestQueue!!.add(mJsonObjectRequest)
    }

    fun getLobbies(activity: LobbyCardsFragment, gameMode:String){
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonObjectRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.ACTIVE_LOBBY_ENDPOINT + "/" + gameMode,
            null,
            Response.Listener<JSONArray> { response ->
                activity.loadLobbies(responseToLobbies(response, activity!!.context!!))
            }, Response.ErrorListener { error ->
                Toast.makeText(activity.context, error.message, Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    private fun responseToLobbies(response: JSONArray, context: Context): ArrayList<Lobby>{
        var lobbies = arrayListOf<Lobby>()
        for(i in 0 until response.length()){
            lobbies.add(Lobby(response.getJSONObject(i), context))
        }
        return lobbies
    }
}
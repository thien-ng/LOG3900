package com.example.client_leger.Controller

import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.Constants
import com.example.client_leger.Fragments.DrawFragment
import com.example.client_leger.Fragments.LobbyFragment

class GameController {

    fun startGame(fragment: LobbyFragment, lobbyName: String){
        val requestQueue = Volley.newRequestQueue(fragment.context)

        val jsonObjectRequest = StringRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.START_GAME_ENDPOINT + lobbyName,
            Response.Listener {
                    fragment.replaceFragment(DrawFragment())
            }, Response.ErrorListener{
                    error ->
                Toast.makeText(
                    fragment.context,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()

            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    fun getUsers(fragment: LobbyFragment, lobbyName: String) {
        val requestQueue = Volley.newRequestQueue(fragment.context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.USERS_LOBBY_ENDPOINT + lobbyName,
            null,
            Response.Listener {response ->
                fragment.loadUsers(response)
            }, Response.ErrorListener{
                    error ->
                Toast.makeText(
                    fragment.context,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()

            }
        )

        requestQueue.add(jsonArrayRequest)
    }
}
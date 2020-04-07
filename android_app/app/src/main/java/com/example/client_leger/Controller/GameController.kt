package com.example.client_leger.Controller

import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.Constants
import com.example.client_leger.Fragments.LobbyFragment
import org.json.JSONObject

class GameController {

    fun startGame(fragment: LobbyFragment, lobbyName: String) {
        val requestQueue = Volley.newRequestQueue(fragment.context)

        val jsonObjectRequest = StringRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.START_GAME_ENDPOINT + lobbyName,
            Response.Listener {response ->
                // do nothing
            }, Response.ErrorListener { error ->
                Toast.makeText(
                    fragment.context,
                    "Not enough players",
                    Toast.LENGTH_SHORT
                ).show()
            })

        requestQueue.add(jsonObjectRequest)
    }

    fun getUsers(fragment: LobbyFragment, lobbyName: String) {
        val requestQueue = Volley.newRequestQueue(fragment.context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.USERS_LOBBY_ENDPOINT + lobbyName,
            null,
            Response.Listener { response ->
                fragment.loadUsers(response)
            }, Response.ErrorListener { error ->
                Toast.makeText(
                    fragment.context,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()

            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    fun leaveGame(fragment: LobbyFragment, body: JSONObject) {
        val mRequestQueue = Volley.newRequestQueue(fragment.context)

        val mJsonObjectRequest = object : StringRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.LEAVE_LOBBY_ENDPOINT,
            Response.Listener {
                fragment.fragmentManager!!.popBackStackImmediate()
            },
            Response.ErrorListener { error ->
                Toast.makeText(fragment.context, error.toString(), Toast.LENGTH_SHORT).show()
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

    fun addBot(fragment: LobbyFragment, body: JSONObject) {
        val mRequestQueue = Volley.newRequestQueue(fragment.context)

        val mJsonObjectRequest = object : StringRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.LOBBY_JOIN_ENDPOINT,
            Response.Listener {
                fragment!!.userListAdapter.addBot(body.getString("username"))
            },
            Response.ErrorListener { error ->
                Toast.makeText(fragment.context, error.toString(), Toast.LENGTH_SHORT).show()
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


    fun removeBot(fragment: LobbyFragment, body: JSONObject) {
        val mRequestQueue = Volley.newRequestQueue(fragment.context)

        val mJsonObjectRequest = object : StringRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.LEAVE_LOBBY_ENDPOINT,
            Response.Listener {
            },
            Response.ErrorListener { error ->
                Toast.makeText(fragment.context, error.toString(), Toast.LENGTH_SHORT).show()
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
}
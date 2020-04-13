package com.example.client_leger.Controller

import android.app.AlertDialog
import android.support.v4.app.FragmentManager
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.Constants
import com.example.client_leger.Fragments.LobbyCardsFragment
import com.example.client_leger.Fragments.LobbyFragment
import com.example.client_leger.R
import org.json.JSONObject


class GameController {

    fun startGame(fragment: LobbyFragment, lobbyName: String) {
        val requestQueue = Volley.newRequestQueue(fragment.context)

        val jsonObjectRequest = StringRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.START_GAME_ENDPOINT + lobbyName,
            Response.Listener {
                // do nothing
            }, Response.ErrorListener {
                Toast.makeText(
                    fragment.context,
                    "Not enough players",
                    Toast.LENGTH_SHORT
                ).show()
            })

        requestQueue.add(jsonObjectRequest)
    }

    fun getUsers(fragment: LobbyFragment, lobbyName: String, mode: String) {
        val requestQueue = Volley.newRequestQueue(fragment.context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.USERS_LOBBY_ENDPOINT + lobbyName,
            null,
            Response.Listener { response ->
                fragment.setView(response, mode)
            }, Response.ErrorListener { error ->
                Toast.makeText(
                    fragment.context,
                    Utils.getErrorMessage(error),
                    Toast.LENGTH_SHORT
                ).show()

            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    fun getOnlineUsers(fragment: LobbyFragment, builder: AlertDialog.Builder, username: String) {
        val requestQueue = Volley.newRequestQueue(fragment.context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.USERS_ONLINE_ENDPOINT,
            null,
            Response.Listener { response ->
                val list = ArrayList<String>()
                for (i in 0 until response.length()) {
                    if (response[i] != username)
                        list.add(response[i] as String);
                }
                val array = arrayOfNulls<String>(list.size)
                builder.setSingleChoiceItems(list.toArray(array), -1) { dialogInterface, i ->
                    fragment.invitePlayer(list[i])
                    dialogInterface.dismiss()
                }
                val mDialog = builder.create()
                mDialog.show()

            }, Response.ErrorListener { error ->
                Toast.makeText(
                    fragment.context,
                    Utils.getErrorMessage(error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    fun invitePlayer(fragment: LobbyFragment, body: JSONObject) {
        val mRequestQueue = Volley.newRequestQueue(fragment.context)

        val mJsonObjectRequest = object : StringRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.INVITE_ENDPOINT,
            Response.Listener {
                Toast.makeText(fragment.context, "Invitation sent", Toast.LENGTH_SHORT).show()
            },
            Response.ErrorListener { error ->
                Toast.makeText(fragment.context, Utils.getErrorMessage(error), Toast.LENGTH_SHORT).show()
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

    fun leaveGame(fragment: LobbyFragment, body: JSONObject, fragmentManager: FragmentManager) {
        val mRequestQueue = Volley.newRequestQueue(fragment.context)

        val mJsonObjectRequest = object : StringRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.LEAVE_LOBBY_ENDPOINT,
            Response.Listener {
                fragmentManager.beginTransaction().replace(R.id.container_view_right, LobbyCardsFragment()).commit()
            },
            Response.ErrorListener { error ->
                Toast.makeText(fragment.context, Utils.getErrorMessage(error), Toast.LENGTH_SHORT).show()
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
            },
            Response.ErrorListener { error ->
                Toast.makeText(fragment.context, Utils.getErrorMessage(error), Toast.LENGTH_SHORT).show()
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


    fun removePlayer(fragment: LobbyFragment, body: JSONObject) {
        val mRequestQueue = Volley.newRequestQueue(fragment.context)

        val mJsonObjectRequest = object : StringRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.LEAVE_LOBBY_ENDPOINT,
            Response.Listener {
            },
            Response.ErrorListener { error ->
                Toast.makeText(fragment.context, Utils.getErrorMessage(error), Toast.LENGTH_SHORT).show()
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
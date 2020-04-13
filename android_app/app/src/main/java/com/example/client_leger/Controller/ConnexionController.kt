package com.example.client_leger

import android.content.Context
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.Constants.Companion.GAME_CHANNEL_ID
import com.example.client_leger.Constants.Companion.LOBBY_CHANNEL_ID
import com.example.client_leger.Controller.Utils
import com.example.client_leger.Fragments.*
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_registration.*
import org.json.JSONArray
import org.json.JSONObject


class ConnexionController {

    fun loginUser(activity: LoginFragment, applicationContext: Context, body: JSONObject){
        val mRequestQueue = Volley.newRequestQueue(applicationContext)

        val mJsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.LOGIN_ENDPOINT,
            null,
            Response.Listener { response ->
                if(response["status"].toString().toInt() == 200) {
                    SocketIO.connect(body.get("username").toString())
                }
                else {
                    Toast.makeText(
                        applicationContext,
                        response["message"].toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    activity.login_button.isEnabled = true
                }
            },
            Response.ErrorListener {error->
                Toast.makeText(
                    applicationContext,
                    Utils.getErrorMessage(error),
                    Toast.LENGTH_SHORT
                ).show()
                activity.login_button.isEnabled = true
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



    fun registerUser(activity: RegisterFragment, applicationContext: Context, body: JSONObject){
        val mRequestQueue = Volley.newRequestQueue(applicationContext)

        val mStringRequest = object : JsonObjectRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.REGISTER_ENDPOINT,
            null,
            Response.Listener { response ->
                if(response["status"].toString().toInt() == 200) {
                    SocketIO.connect(body.get("username").toString())
                    activity.register_button.isEnabled = true
                }
                else {
                    Toast.makeText(
                        applicationContext,
                        response["message"].toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    activity.register_button.isEnabled = true
                }
            },
            Response.ErrorListener {error ->
                Toast.makeText(
                    applicationContext,
                    Utils.getErrorMessage(error),
                    Toast.LENGTH_SHORT
                ).show()
                activity.register_button.isEnabled = true
            }) {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                return body.toString().toByteArray()
            }
        }
        mRequestQueue!!.add(mStringRequest)
    }

    fun showLoadHistoryButtonIfPreviousMessages(activity: ChatFragment, messageRoute: String) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + messageRoute,
            null,
            Response.Listener { response ->
                if (response.length() > 0) {
                    activity.showLoadHistoryButton()
                } else {
                    activity.hideLoadHistoryButton()
                }
            }, Response.ErrorListener {
                    error ->
                Toast.makeText(
                    activity.context,
                    Utils.getErrorMessage(error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    fun loadChatHistory(activity: ChatFragment) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + "/chat/messages/" + activity.channelId,
            null,
            Response.Listener { response ->
                activity.messageAdapter.clear()
                activity.receiveMessages(activity.messageAdapter, activity.username, response, activity.channelId)
                activity.recyclerViewChatLog.scrollToPosition(activity.messageAdapter.itemCount -1)
            }, Response.ErrorListener {
                    error ->
                Toast.makeText(
                    activity.context,
                    Utils.getErrorMessage(error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    fun loadLobbyChatHistory(activity: ChatFragment) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + "/game/lobby/messages/" + activity.lobbyName,
            null,
            Response.Listener { response ->
                activity.messageAdapter.clear()
                activity.receiveMessages (
                    activity.messageAdapter,
                    activity.username,
                    response,
                    activity.channelId,
                    false
                )
                activity.recyclerViewChatLog.scrollToPosition(activity.messageAdapter.itemCount -1)
            }, Response.ErrorListener {
                    error ->
                Toast.makeText (
                    activity.context,
                    Utils.getErrorMessage(error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    fun loadGameChatHistory(activity: ChatFragment) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + "/game/arena/messages/" + activity.username,
            null,
            Response.Listener { response ->
                activity.messageAdapter.clear()
                activity.receiveMessages (
                    activity.messageAdapter,
                    activity.username, response,
                    activity.channelId,
                    false
                )
                activity.recyclerViewChatLog.scrollToPosition(activity.messageAdapter.itemCount -1)
            }, Response.ErrorListener {
                    error ->
                Toast.makeText (
                    activity.context,
                    Utils.getErrorMessage(error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    fun joinChannel(activity: ChatFragment, channelId: String) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.PUT,
            Constants.SERVER_URL + "/chat/channels/join/" + activity.username + "/" + channelId ,
            null,
            Response.Listener {
                activity.setChannel(channelId)
            },Response.ErrorListener{ error ->
                Toast.makeText(activity.context, Utils.getErrorMessage(error), Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    fun leaveChannel(activity: ChatFragment, channelId: String) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.DELETE,
            Constants.SERVER_URL + "/chat/channels/leave/" + activity.username + "/" + channelId ,
            null,
            Response.Listener {
                if (activity.channelId == channelId) {
                    activity.setChannel(Constants.DEFAULT_CHANNEL_ID)
                }
                else {
                    activity.loadChannels()
                }
            },Response.ErrorListener{ error ->
                Toast.makeText(activity.context, Utils.getErrorMessage(error), Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    fun createChannel(activity: ChatFragment, channelId: String) {
        joinChannel(activity, channelId)
    }

    private fun checkForSubChannelsToAddOrRemove(activity: ChatFragment, response: JSONArray) {
        val channelsToRemove = mutableListOf<Item<ViewHolder>>()
        for (channel in 0 until activity.channelAdapter.itemCount) {
            val channelName = activity.channelAdapter.getItem(channel).toString()
            if (channelName == GAME_CHANNEL_ID && activity.inGame ||
                channelName == LOBBY_CHANNEL_ID && activity.inLobby)
                continue
            var isStillThere = false
            for (i in 0 until response.length()) {
                if (channelName == response.getJSONObject(i).getString("id")) {
                    isStillThere = true
                    break
                }
            }
            if (!isStillThere) {
                channelsToRemove.add(activity.channelAdapter.getItem(channel))
            }
        }

        for (channelToRemove in channelsToRemove) {
            activity.channelAdapter.remove(channelToRemove)
        }

        val channelsToAdd = mutableListOf<Int>()
        for (i in 0 until response.length()) {
            val channelName = response.getJSONObject(i).getString("id")
            var isThere = false
            for (channel in 0 until activity.channelAdapter.itemCount) {
                if (channelName == activity.channelAdapter.getItem(channel).toString()) {
                    isThere = true
                    break
                }
            }
            if (!isThere) {
                channelsToAdd.add(i)
            }
        }

        for (channelIndexToAdd in channelsToAdd) {
            activity.channelAdapter.add(ChannelItem(
                response.getJSONObject(channelIndexToAdd).getString("id"),
                true,
                this,
                activity)
            )
        }
    }

    private fun checkForUnSubChannelsToAddOrRemove(activity: ChatFragment, response: JSONArray) {
        val channelsToRemove = mutableListOf<Item<ViewHolder>>()
        for (channel in 0 until activity.notSubChannelAdapter.itemCount) {
            val channelName = activity.notSubChannelAdapter.getItem(channel).toString()
            var isStillThere = false
            for (i in 0 until response.length()) {
                if (channelName == response.getJSONObject(i).getString("id")) {
                    isStillThere = true
                    break
                }
            }
            if (!isStillThere) {
                channelsToRemove.add(activity.notSubChannelAdapter.getItem(channel))
            }
        }

        for (channelToRemove in channelsToRemove) {
            activity.notSubChannelAdapter.remove(channelToRemove)
        }

        val channelsToAdd = mutableListOf<Int>()
        for (i in 0 until response.length()) {
            val channelName = response.getJSONObject(i).getString("id")
            var isThere = false
            for (channel in 0 until activity.notSubChannelAdapter.itemCount) {
                if (channelName == activity.notSubChannelAdapter.getItem(channel).toString()) {
                    isThere = true
                    break
                }
            }
            if (!isThere) {
                channelsToAdd.add(i)
            }
        }

        for (i in channelsToAdd) {
            activity.notSubChannelAdapter.add(
                ChannelItem(response.getJSONObject(i).getString("id"),
                    false,
                    this,
                    activity)
            )
        }
    }

    fun loadChannels(activity: ChatFragment, search: String? = null) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        if (search.isNullOrBlank()) {
            val subRequest = JsonArrayRequest(
                Request.Method.GET,
                Constants.SERVER_URL + "/chat/channels/sub/" + activity.username ,
                null,
                Response.Listener{response ->
                    activity.channelAdapter.notifyDataSetChanged()
                    checkForSubChannelsToAddOrRemove(activity, response)
                },Response.ErrorListener{ error ->
                    Toast.makeText(activity.context, Utils.getErrorMessage(error), Toast.LENGTH_SHORT).show()
                }
            )
            requestQueue.add(subRequest)

            val requestQueueNotSub = Volley.newRequestQueue(activity.context)
            val notSubRequest = JsonArrayRequest(
                Request.Method.GET,
                Constants.SERVER_URL + "/chat/channels/notsub/" + activity.username ,
                null,
                Response.Listener {response ->
                    activity.notSubChannelAdapter.notifyDataSetChanged()
                    checkForUnSubChannelsToAddOrRemove(activity, response)
                },Response.ErrorListener{ error ->
                    Toast.makeText(activity.context, Utils.getErrorMessage(error), Toast.LENGTH_SHORT).show()
                }
            )
            requestQueueNotSub.add(notSubRequest)
        }
        else {
            val subRequest = JsonArrayRequest(
                Request.Method.GET,
                Constants.SERVER_URL + "/chat/channels/search/" + activity.username + "/" + search,
                null,
                Response.Listener {response ->
                    activity.channelAdapter.clear()
                    activity.notSubChannelAdapter.clear()

                    for (i in 0 until response.length()) {
                        val channel = response.getJSONObject(i)
                        if (channel.getString("sub") == "true") {
                            activity.channelAdapter.add(ChannelItem(channel.getString("id"), true, this, activity))
                            activity.channelAdapter.setOnItemClickListener { item, _ ->
                                activity.setChannel(item.toString())
                            }
                        }
                        else {
                            activity.notSubChannelAdapter.add(ChannelItem(channel.getString("id"), false, this, activity))
                            activity.notSubChannelAdapter.setOnItemClickListener { item, _ ->
                                joinChannel(activity, item.toString())
                            }
                        }
                    }
                },Response.ErrorListener{ error ->
                    Toast.makeText(activity.context, Utils.getErrorMessage(error), Toast.LENGTH_SHORT).show()
                }
            )
            requestQueue.add(subRequest)
        }
    }
}
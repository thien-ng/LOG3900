package com.example.client_leger

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.Fragments.*
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
                    activity.login_button.isEnabled = true
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
            Response.ErrorListener {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong...",
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
            Response.ErrorListener {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong...",
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

    fun loadChatHistory(activity: ChatFragment) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + "/chat/messages/" + activity.channelId,
            null,
            Response.Listener<JSONArray>{ response ->
                activity.receiveMessages(activity.messageAdapter, activity.username, response)
                activity.recyclerViewChatLog.scrollToPosition(activity.messageAdapter.itemCount -1)
            },Response.ErrorListener{
                    error ->
                Toast.makeText(
                    activity.context,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()

            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    private fun joinChannel(activity: ChatFragment, channelId: String) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.PUT,
            Constants.SERVER_URL + "/chat/channels/join/" + activity.username + "/" + channelId ,
            null,
            Response.Listener<JSONObject>{
                activity.setChannel(channelId)
            },Response.ErrorListener{ error ->
                Toast.makeText(activity.context, error.message, Toast.LENGTH_SHORT).show()
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
            Response.Listener<JSONObject>{
                if (activity.channelId == channelId) {
                    activity.setChannel(Constants.DEFAULT_CHANNEL_ID)
                }
                else {
                    activity.loadChannels()
                }
            },Response.ErrorListener{ error ->
                Toast.makeText(activity.context, error.message, Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }

    fun createChannel(activity: ChatFragment, channelId: String) {
        if (channelId.isNotEmpty()) {
            joinChannel(activity, channelId)
        }
    }

    fun loadChannels(activity: ChatFragment, search: String? = null) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        if (search.isNullOrBlank()){
            val subRequest = JsonArrayRequest(
                Request.Method.GET,
                Constants.SERVER_URL + "/chat/channels/sub/" + activity.username ,
                null,
                Response.Listener<JSONArray>{response ->
                    activity.channelAdapter.clear()
                    for (i in 0 until response.length()) {
                        val channelId = response.getJSONObject(i)
                        activity.channelAdapter.add(ChannelItem(channelId.getString("id"), true, this, activity))

                        activity.channelAdapter.setOnItemClickListener { item, _ ->

                            activity.setChannel(item.toString())
                        }
                    }
                },Response.ErrorListener{ error ->
                    Toast.makeText(activity.context, error.message, Toast.LENGTH_SHORT).show()
                }
            )
            requestQueue.add(subRequest)

            val requestQueueNotSub = Volley.newRequestQueue(activity.context)
            val notSubRequest = JsonArrayRequest(
                Request.Method.GET,
                Constants.SERVER_URL + "/chat/channels/notsub/" + activity.username ,
                null,
                Response.Listener<JSONArray>{response ->
                    activity.notSubChannelAdapter.clear()
                    for (i in 0 until response.length()) {
                        val channelId = response.getJSONObject(i)
                        activity.notSubChannelAdapter.add(ChannelItem(channelId.getString("id"), false, this, activity))
                        activity.notSubChannelAdapter.setOnItemClickListener { item, _ ->
                            joinChannel(activity, item.toString())
                        }
                    }
                },Response.ErrorListener{ error ->
                    Log.w("socket", "load")
                    Toast.makeText(activity.context, error.message, Toast.LENGTH_SHORT).show()
                }
            )
            requestQueueNotSub.add(notSubRequest)
        }
        else {
            val subRequest = JsonArrayRequest(
                Request.Method.GET,
                Constants.SERVER_URL + "/chat/channels/search/" + activity.username + "/" + search,
                null,
                Response.Listener<JSONArray>{response ->
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
                    Toast.makeText(activity.context, error.message, Toast.LENGTH_SHORT).show()
                }
            )
            requestQueue.add(subRequest)
        }
    }

}
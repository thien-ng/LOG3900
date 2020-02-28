package com.example.client_leger.Fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.*
import com.example.client_leger.Communication.Communication
import com.example.client_leger.Constants.Companion.DEFAULT_CHANNEL_ID
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_chat.view.*
import org.json.JSONArray
import org.json.JSONObject

class ChatFragment: Fragment() {

    private var channelId: String = DEFAULT_CHANNEL_ID
    private lateinit var username: String
    private lateinit var recyclerViewChannels: RecyclerView
    private lateinit var recyclerViewNotSubChannels: RecyclerView
    private lateinit var recyclerViewChatLog: RecyclerView
    private lateinit var messageAdapter: GroupAdapter<ViewHolder>
    private lateinit var channelAdapter: GroupAdapter<ViewHolder>
    private lateinit var notSubChannelAdapter: GroupAdapter<ViewHolder>
    private lateinit var textViewChannelName: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerViewChannels = v.recyclerView_channels
        recyclerViewNotSubChannels = v.recyclerView_notSubChannels
        recyclerViewChatLog = v.recyclerView_chat_log
        textViewChannelName = v.textView_channelName
        messageAdapter = GroupAdapter()
        username = activity!!.intent.getStringExtra("username")

        recyclerViewChannels.setHasFixedSize(true)
        channelAdapter = GroupAdapter()
        val manager = LinearLayoutManager(this.context)
        recyclerViewChannels.layoutManager = manager
        recyclerViewChannels.adapter = channelAdapter

        recyclerViewNotSubChannels.setHasFixedSize(true)
        notSubChannelAdapter = GroupAdapter()
        val managerNotSub = LinearLayoutManager(this.context)
        recyclerViewNotSubChannels.layoutManager = managerNotSub
        recyclerViewNotSubChannels.adapter = notSubChannelAdapter

        loadChannels(channelAdapter, notSubChannelAdapter)

        val fArray = arrayOfNulls<InputFilter>(1)
        fArray[0] = InputFilter.LengthFilter(Constants.MESSAGE_MAX_LENGTH)
        v.chat_message_editText.filters = fArray

        loadChatHistory(channelId, messageAdapter, v.recyclerView_chat_log, username)

        setChannel(channelId)

        v.searchView_channelSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                loadChannels(channelAdapter, notSubChannelAdapter, newText)
                return false
            }
        })

        v.chat_message_editText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (v.chat_message_editText.text.trim().isNotEmpty()) {
                    val message = buildMessage(username, v.chat_message_editText, channelId)
                    SocketIO.sendMessage("chat", message)
                }
                return@OnKeyListener true
            }
            false
        })

        v.chat_send_button.setOnClickListener {
            if (v.chat_message_editText.text.trim().isNotEmpty()) {
                val message = buildMessage(username, v.chat_message_editText, channelId)
                SocketIO.sendMessage("chat", message)
            }
        }

        v.disconnect_button.setOnClickListener {
            SocketIO.disconnect()
            val intent = Intent(activity, LogPageActivity::class.java)
            startActivity(intent)
        }

        Communication.getChatMessageListener().subscribe{receptMes ->
            val messages = JSONArray()
            messages.put(receptMes)
            receiveMessages(messageAdapter, username, messages)
            v.recyclerView_chat_log.smoothScrollToPosition(messageAdapter.itemCount)
        }

        v.recyclerView_chat_log.adapter = messageAdapter

        return v
    }

    private fun setChannel(newChannelId: String) {
        messageAdapter.clear()
        channelId = newChannelId
        loadChatHistory(channelId, messageAdapter, recyclerViewChatLog, username)
        textViewChannelName.text = channelId
    }

    private fun buildMessage(username: String, message: EditText, chan_id: String): JSONObject {
        val obj = JSONObject()
        obj.put("username", username)
        obj.put("channel_id", chan_id)
        obj.put("content", message.text.toString())

        message.text.clear()

        return obj
    }

    private fun receiveMessages(adapter: GroupAdapter<ViewHolder>, curUser: String, messages: JSONArray){
        for (i in 0 until messages.length()){
            val message = messages.getJSONObject(i)
            val username = message.getString("username")
            val content = message.getString("content")
            val time = message.getString("time")

            activity!!.runOnUiThread {
                if(curUser != username){
                    adapter.add(ChatItemReceived(content, username, time))
                }
                else {
                    adapter.add(ChatItemSent(content, time))
                }
            }
        }
    }

    private fun loadChatHistory(channelId: String, adapter: GroupAdapter<ViewHolder>, recyclerView: RecyclerView, curUser: String){
        val requestQueue = Volley.newRequestQueue(context)

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + "/chat/messages/" + channelId,
            null,
             Response.Listener<JSONArray>{response ->
                    receiveMessages(adapter, curUser, response)
                    recyclerView.scrollToPosition(adapter.itemCount -1)
            },Response.ErrorListener{ error ->
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonArrayRequest)
    }

    private fun joinChannel(channelId: String) {
        val requestQueue = Volley.newRequestQueue(context)

        val jsonArrayRequest = JsonObjectRequest(
            Request.Method.PUT,
            Constants.SERVER_URL + "/chat/channels/join/" + username + "/" + channelId ,
            null,
            Response.Listener<JSONObject>{
                loadChannels(channelAdapter, notSubChannelAdapter)
                setChannel(channelId)
            },Response.ErrorListener{ error ->
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonArrayRequest)
    }

    private fun loadChannels(adapter: GroupAdapter<ViewHolder>, notSubAdapter: GroupAdapter<ViewHolder>, search: String? = null) {
        val requestQueue = Volley.newRequestQueue(context)

        if (search.isNullOrBlank()){
            val subRequest = JsonArrayRequest(
                Request.Method.GET,
                Constants.SERVER_URL + "/chat/channels/" + "sub/$username" ,
                null,
                Response.Listener<JSONArray>{response ->
                    adapter.clear()
                    for (i in 0 until response.length()) {
                        val channelId = response.getJSONObject(i)
                        adapter.add(ChannelItem(channelId.getString("id")))
                        adapter.setOnItemClickListener { item, _ ->
                            setChannel(item.toString())
                        }
                    }
                },Response.ErrorListener{ error ->
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            )
            requestQueue.add(subRequest)

            val requestQueueNotSub = Volley.newRequestQueue(context)
            val notSubRequest = JsonArrayRequest(
                Request.Method.GET,
                Constants.SERVER_URL + "/chat/channels/" + "notsub/$username" ,
                null,
                Response.Listener<JSONArray>{response ->
                    notSubAdapter.clear()
                    for (i in 0 until response.length()) {
                        val channelId = response.getJSONObject(i)
                        notSubAdapter.add(ChannelItem(channelId.getString("id")))
                        notSubAdapter.setOnItemClickListener { item, _ ->
                            joinChannel(item.toString())
                        }
                    }
                },Response.ErrorListener{ error ->
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            )
            requestQueueNotSub.add(notSubRequest)
        }
        else {
            val subRequest = JsonArrayRequest(
                Request.Method.GET,
                Constants.SERVER_URL + "/chat/channels/search/$username/" + search,
                null,
                Response.Listener<JSONArray>{response ->
                    adapter.clear()
                    notSubAdapter.clear()

                    for (i in 0 until response.length()) {
                        val channel = response.getJSONObject(i)
                        if (channel.getString("sub") == "true") {
                            adapter.add(ChannelItem(channel.getString("id")))
                            adapter.setOnItemClickListener { item, _ ->
                                setChannel(item.toString())
                            }
                        }
                        else {
                            notSubAdapter.add(ChannelItem(channel.getString("id")))
                            notSubAdapter.setOnItemClickListener { item, _ ->
                                joinChannel(item.toString())
                            }
                        }
                    }
                },Response.ErrorListener{ error ->
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            )
            requestQueue.add(subRequest)
        }
    }
}
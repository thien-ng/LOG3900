package com.example.client_leger.Fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.*
import com.example.client_leger.Communication.Communication
import com.example.client_leger.Constants.Companion.DEFAULT_CHANNEL_ID
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_chat.view.*
import org.json.JSONArray
import org.json.JSONObject

class ChatFragment: Fragment() {

    var channelId: String = DEFAULT_CHANNEL_ID
    lateinit var username: String
    lateinit var recyclerView_channels: RecyclerView
    lateinit var recyclerView_chat_log: RecyclerView
    lateinit var textView_channelName: TextView
    lateinit var messageAdapter: GroupAdapter<ViewHolder>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerView_channels = v.recyclerView_channels
        recyclerView_chat_log = v.recyclerView_chat_log
        textView_channelName = v.textView_channelName
        messageAdapter = GroupAdapter()
        username = activity!!.intent.getStringExtra("username")

        SocketIO.connect(username)

        val fArray = arrayOfNulls<InputFilter>(1)
        fArray[0] = InputFilter.LengthFilter(Constants.MESSAGE_MAX_LENGTH)
        v.chat_message_editText.filters = fArray

        loadChatHistory(channelId, messageAdapter, v.recyclerView_chat_log, username)

        setChannel(channelId)

        v.chat_message_editText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (v.chat_message_editText.text.trim().length > 0) {
                    val message = buildMessage(username, v.chat_message_editText, channelId)
                    SocketIO.sendMessage("chat", message)
                }
                return@OnKeyListener true
            }
            false
        })

        v.chat_send_button.setOnClickListener {
            if (v.chat_message_editText.text.trim().length > 0) {
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
        Log.w("socket", "switching channel to: $newChannelId")
        messageAdapter.clear()
        channelId = newChannelId
        textView_channelName.text = channelId
        val adapterChannels = GroupAdapter<ViewHolder>()
        val manager = LinearLayoutManager(this.context)
        recyclerView_channels.layoutManager = manager
        recyclerView_channels.setHasFixedSize(true)
        recyclerView_channels.adapter = adapterChannels
        loadChannels(adapterChannels)
        loadChatHistory(channelId, messageAdapter, recyclerView_chat_log, username)
        textView_channelName.text = channelId
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
            var message = messages.getJSONObject(i)
            val username = message.getString("username")
            val content = message.getString("content")
            val time = message.getString("time")

            activity!!.runOnUiThread {
                if(curUser != username){
                    adapter.add(ChatItemReceived(content, curUser, time))
                }
                else {
                    adapter.add(ChatItemSent(content, time))
                }
            }
        }
    }

    private fun loadChatHistory( channelId: String, adapter: GroupAdapter<ViewHolder>, recyclerView: RecyclerView, curUser: String){
        val requestQueue = Volley.newRequestQueue(context)

        var jsonArrayRequest:JsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + "/chat/messages/" + channelId,
            null,
             Response.Listener<JSONArray>{response ->
                    receiveMessages(adapter, curUser, response)
                    recyclerView.scrollToPosition(adapter.itemCount -1)
            },Response.ErrorListener{
               error ->
                    //Do something when error occurred
                Toast.makeText(
                    context,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()

            }
        )

        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonArrayRequest);
    }

    private fun loadChannels(adapter: GroupAdapter<ViewHolder>) {
        val requestQueue = Volley.newRequestQueue(context)
        var jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + "/chat/channels/all" ,
            null,
            Response.Listener<JSONArray>{response ->
                for (i in 0 until response.length()) {
                    var channelId = response.getJSONObject(i)
                    adapter.add(ChannelItem(channelId.getString("id")))
                    adapter.setOnItemClickListener { item, view ->
                        setChannel(item.toString())
                    }
                }
            },Response.ErrorListener{
                    error ->
                //Do something when error occurred
                Toast.makeText(
                    context,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()

            }
        )

        // Add JsonArrayRequest to the RequestQueue
        requestQueue.add(jsonArrayRequest);
    }
}
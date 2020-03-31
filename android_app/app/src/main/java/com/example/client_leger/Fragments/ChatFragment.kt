package com.example.client_leger.Fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.InputFilter
import android.view.*
import android.widget.*
import com.example.client_leger.*
import com.example.client_leger.Communication.Communication
import com.example.client_leger.Constants.Companion.DEFAULT_CHANNEL_ID
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.popup_create_channel.view.*
import kotlinx.android.synthetic.main.fragment_chat.view.textView_channelName
import org.json.JSONArray
import org.json.JSONObject


class ChatFragment: Fragment() {

    var channelId: String = DEFAULT_CHANNEL_ID
    lateinit var username: String
    private lateinit var recyclerViewChannels: RecyclerView
    private lateinit var recyclerViewNotSubChannels: RecyclerView
    lateinit var recyclerViewChatLog: RecyclerView
    lateinit var messageAdapter: GroupAdapter<ViewHolder>
    lateinit var channelAdapter: GroupAdapter<ViewHolder>
    lateinit var notSubChannelAdapter: GroupAdapter<ViewHolder>
    private lateinit var textViewChannelName: TextView
    private var controller = ConnexionController()

    lateinit var chatListener: Disposable
    lateinit var channelListener: Disposable
    lateinit var startGameSub: Disposable;

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

        val fArray = arrayOfNulls<InputFilter>(1)
        fArray[0] = InputFilter.LengthFilter(Constants.MESSAGE_MAX_LENGTH)
        v.chat_message_editText.filters = fArray

        loadChannels()
        controller.loadChatHistory(this)
        textViewChannelName.text = channelId

        v.searchView_channelSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                loadChannels(newText)
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

        v.imageButton_createChannel.setOnClickListener {
            onButtonShowPopupWindowClick(inflater, v)
        }

        v.disconnect_button.setOnClickListener {
            activity!!.onBackPressed()
        }

        chatListener = Communication.getChatMessageListener().subscribe{receptMes ->
            val messages = JSONArray()
            messages.put(receptMes)
            receiveMessages(messageAdapter, username, messages)
            v.recyclerView_chat_log.smoothScrollToPosition(messageAdapter.itemCount)
        }

        startGameSub = Communication.getGameStartListener().subscribe{ res ->
            addGameChannel()
        }

        channelListener = Communication.getChannelUpdateListener().subscribe{ channel ->
            notSubChannelAdapter.add(ChannelItem(channel, false, controller, this))
        }

        v.recyclerView_chat_log.adapter = messageAdapter

        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        chatListener.dispose()
        channelListener.dispose()
        startGameSub.dispose()
    }

    fun addGameChannel() {
        channelAdapter.add(ChannelItem("", true, controller, this))
    }

    private fun onButtonShowPopupWindowClick(inflater: LayoutInflater, view: View?) {
        val popupView = inflater.inflate(R.layout.popup_create_channel, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

        popupView.setOnTouchListener { _, _ ->
            popupWindow.dismiss()
            true
        }

        popupView.create_channel_button.setOnClickListener {
            var name = popupView.textInput_channelNameToCreate.text.toString()
            name = name.trim()

            if ( name.length <= 20 && channelId.isNotEmpty() ) {
                controller.createChannel(this, name)
                popupWindow.dismiss()
            } else {
                Toast.makeText(
                    this.context,
                    "Channel names cannot exceed 20 characters or be empty.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun loadChannels(search: String? = null){
        controller.loadChannels(this, search)
    }

    fun setChannel(newChannelId: String) {
        if (newChannelId == "") {
            messageAdapter.clear()
            textViewChannelName.text = "Game channel"
        } else if (channelId != newChannelId) {
            loadChannels()
            messageAdapter.clear()
            channelId = newChannelId
            controller.loadChatHistory(this)
            textViewChannelName.text = channelId
        }
    }

    private fun buildMessage(username: String, message: EditText, chan_id: String): JSONObject {
        val obj = JSONObject()
        obj.put("username", username)
        obj.put("channel_id", chan_id)
        obj.put("content", message.text.toString())

        message.text.clear()

        return obj
    }

    fun receiveMessages(adapter: GroupAdapter<ViewHolder>, curUser: String, messages: JSONArray){
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
}
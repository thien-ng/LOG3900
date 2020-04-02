package com.example.client_leger.Fragments

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
import com.example.client_leger.Constants.Companion.GAME_CHANNEL_ID
import com.example.client_leger.Constants.Companion.LOBBY_CHANNEL_ID
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
    var inGame: Boolean = false

    private lateinit var chatListener: Disposable
    private lateinit var channelListener: Disposable
    private lateinit var startGameSub: Disposable
    private lateinit var endGameSub: Disposable
    private lateinit var gameChatSub: Disposable
    private lateinit var lobbyChatSub: Disposable
    private lateinit var lobbyNotifSub: Disposable

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
                sendInput(v)
                return@OnKeyListener true
            }
            false
        })

        v.chat_send_button.setOnClickListener {
            sendInput(v)
        }

        v.imageButton_createChannel.setOnClickListener {
            onButtonShowPopupWindowClick(inflater, v)
        }

        v.disconnect_button.setOnClickListener {
            activity!!.onBackPressed()
        }

        chatListener = Communication.getChatMessageListener().subscribe{ receptMes ->
            val messages = JSONArray()
            messages.put(receptMes)
            receiveMessages(messageAdapter, username, messages)
            v.recyclerView_chat_log.smoothScrollToPosition(messageAdapter.itemCount)
        }

        startGameSub = Communication.getGameStartListener().subscribe{
            inGame = true
            addGameChannel()
            activity!!.runOnUiThread {
                setChannel(GAME_CHANNEL_ID)
            }
        }

        endGameSub = Communication.getEndGameListener().subscribe{
            inGame = false

            activity!!.runOnUiThread {
                if (channelId == GAME_CHANNEL_ID) {
                    setChannel(DEFAULT_CHANNEL_ID)
                } else {
                    loadChannels()
                }
            }
        }

        gameChatSub = Communication.getGameChatListener().subscribe { mes ->
            receiveGameMessage(mes)
            v.recyclerView_chat_log.smoothScrollToPosition(messageAdapter.itemCount)
        }

        lobbyChatSub = Communication.getLobbyChatListener().subscribe { mes ->
            receiveLobbyMessage(mes)
            v.recyclerView_chat_log.smoothScrollToPosition(messageAdapter.itemCount)
        }

        lobbyNotifSub = Communication.getLobbyUpdateListener().subscribe { mes ->
            if (mes.getString("type") == "create") {
                val user = mes.getJSONArray("users").getString(0)
                //First user should always be the lobby creator.

                if (username == user) {
                    addLobbyChannel()
                    activity!!.runOnUiThread {
                        setChannel(LOBBY_CHANNEL_ID)
                    }
                }
            }
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
        gameChatSub.dispose()
        lobbyChatSub.dispose()
        lobbyNotifSub.dispose()
        endGameSub.dispose()
    }

    private fun addGameChannel() {
        channelAdapter.add(ChannelItem(GAME_CHANNEL_ID, true, controller, this))
    }

    private fun addLobbyChannel() {
        channelAdapter.add(ChannelItem(LOBBY_CHANNEL_ID, true, controller, this))
    }

    private fun sendInput(v: View) {
        if (v.chat_message_editText.text.trim().isNotEmpty()) {
            if(channelId != GAME_CHANNEL_ID) {
                val message = buildMessage(username, v.chat_message_editText, channelId)
                SocketIO.sendMessage("chat", message)
            } else {
                val obj = buildGameplayMessage(username, v.chat_message_editText)
                SocketIO.sendMessage("gameplay", obj)
                messageAdapter.add(GameChatItemSent(v.chat_message_editText.text.toString().trim()))
            }

            v.chat_message_editText.text.clear()
        }
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

            if ( name.length <= 20 && name.isNotEmpty() && name != GAME_CHANNEL_ID) {
                controller.createChannel(this, name)
                popupWindow.dismiss()
            } else {
                Toast.makeText(
                    this.context,
                    "Channel names cannot exceed 20 characters, be empty, or named $GAME_CHANNEL_ID or $LOBBY_CHANNEL_ID",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun loadChannels(search: String? = null){
        controller.loadChannels(this, search)
    }

    fun setChannel(newChannelId: String) {
        loadChannels()
        messageAdapter.clear()
        channelId = newChannelId
        textViewChannelName.text = channelId

        if (newChannelId != GAME_CHANNEL_ID) {
            controller.loadChatHistory(this)
        }
    }

    private fun buildMessage(username: String, message: EditText, chan_id: String): JSONObject {
        val obj = JSONObject()
        obj.put("username", username)
        obj.put("channel_id", chan_id)
        obj.put("content", message.text.toString())

        return obj
    }

    private fun buildGameplayMessage(username: String, message: EditText): JSONObject {
        val obj = JSONObject()
        obj.put("event", "chat")
        obj.put("username", username)
        obj.put("content", message.text.toString().trim())

        return obj
    }

    private fun receiveGameMessage(mes: JSONObject) {
        val user = mes.getString("username")
        val content = mes.getString("content")
        val isServer = mes.getBoolean("isServer")

        activity!!.runOnUiThread {
            if (channelId == GAME_CHANNEL_ID && user != username) {
                if (!isServer) {
                    messageAdapter.add(GameChatItemReceived(content, user))
                } else {
                    messageAdapter.add(GameServerChatItemReceived(content))
                }
            }
        }
    }

    private fun receiveLobbyMessage(mes: JSONObject) {
        //todo
    }

    fun receiveMessages(adapter: GroupAdapter<ViewHolder>, curUser: String, messages: JSONArray, channel: String? = null){
        for (i in 0 until messages.length()){
            val message = messages.getJSONObject(i)

            if (channel != null) {
                if (channel != channelId) {
                    continue
                }
            } else if (message.getString("channel_id") != channelId) {
                continue
            }

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
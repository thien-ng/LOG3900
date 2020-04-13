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
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.popup_create_channel.view.*
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
    var lobbyName = ""
    var inGame: Boolean = false
    var inLobby: Boolean = false
    private lateinit var v: View

    private lateinit var chatListener: Disposable
    private lateinit var channelAddedListener: Disposable
    private lateinit var channelRemovedListener: Disposable
    private lateinit var startGameSub: Disposable
    private lateinit var endGameSub: Disposable
    private lateinit var gameChatSub: Disposable
    private lateinit var lobbyChatSub: Disposable
    private lateinit var lobbyNotifSub: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerViewChannels = v.recyclerView_channels
        recyclerViewNotSubChannels = v.recyclerView_notSubChannels
        recyclerViewChatLog = v.recyclerView_chat_log
        textViewChannelName = v.textView_channelName

        messageAdapter = GroupAdapter()
        val layoutManager = LinearLayoutManager(this.context)
        layoutManager.stackFromEnd = true
        recyclerViewChatLog.layoutManager = layoutManager
        username = activity!!.intent.getStringExtra("username")

        recyclerViewChannels.setHasFixedSize(true)
        channelAdapter = GroupAdapter()
        channelAdapter.setOnItemClickListener { item, _ -> setChannel(item.toString()) }
        val manager = LinearLayoutManager(this.context)
        recyclerViewChannels.layoutManager = manager
        recyclerViewChannels.adapter = channelAdapter

        recyclerViewNotSubChannels.setHasFixedSize(true)
        notSubChannelAdapter = GroupAdapter()
        notSubChannelAdapter.setOnItemClickListener { item, _ -> controller.joinChannel(this, item.toString()) }
        val managerNotSub = LinearLayoutManager(this.context)
        recyclerViewNotSubChannels.layoutManager = managerNotSub
        recyclerViewNotSubChannels.adapter = notSubChannelAdapter

        val fArray = arrayOfNulls<InputFilter>(1)
        fArray[0] = InputFilter.LengthFilter(Constants.MESSAGE_MAX_LENGTH)
        v.chat_message_editText.filters = fArray

        loadChannels()

        textViewChannelName.text = channelId

        v.button_load_chat_history.setOnClickListener {
            activity!!.runOnUiThread {
                when (channelId) {
                    GAME_CHANNEL_ID -> {
                        controller.loadGameChatHistory(this)
                    }
                    LOBBY_CHANNEL_ID -> {
                        controller.loadLobbyChatHistory(this)
                    }
                    else -> {
                        controller.loadChatHistory(this)
                    }
                }

                hideLoadHistoryButton()
            }
        }

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
            onButtonShowPopupWindowClick(inflater)
        }

        v.disconnect_button.setOnClickListener {
            activity!!.onBackPressed()
        }

        v.collapse.setOnClickListener {
            v.channelView.visibility = View.GONE
            v.expand.visibility = View.VISIBLE
            v.expand.isEnabled = true
        }
        v.expand.setOnClickListener {
            v.expand.isEnabled = false
            v.expand.visibility = View.GONE
            v.channelView.visibility = View.VISIBLE
        }
        chatListener = Communication.getChatMessageListener().subscribe{ receptMes ->
            val messages = JSONArray()
            messages.put(receptMes)
            receiveMessages(messageAdapter, username, messages)
            v.recyclerView_chat_log.smoothScrollToPosition(messageAdapter.itemCount)
        }

        startGameSub = Communication.getGameStartListener().subscribe{
            inGame = true
            inLobby = false
            addGameChannel()
            setChannel(GAME_CHANNEL_ID)
        }

        endGameSub = Communication.getEndGameListener().subscribe{
            inGame = false

            if (channelId == GAME_CHANNEL_ID) {
                setChannel(DEFAULT_CHANNEL_ID)
            } else {
                loadChannels()
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
            val type = if (mes.isNull("type")) "" else mes.getString("type")
            val firstUser = if (mes.isNull("usernames")) "" else mes.getJSONArray("usernames").getString(0)
            val user = if (mes.isNull("username")) "" else mes.getString("username")
            val lobby = if (mes.isNull("lobbyName")) "" else mes.getString("lobbyName")

            if (type == "create") {
                if (username == firstUser) {
                    inLobby = true
                    lobbyName = lobby
                    addLobbyChannel()
                    setChannel(LOBBY_CHANNEL_ID)
                }
            } else if (type == "join") {
                if (user == username) {
                    inLobby = true
                    lobbyName = lobby
                    addLobbyChannel()
                    setChannel(LOBBY_CHANNEL_ID)
                }
            } else if (type == "delete") {
                if (lobby == lobbyName) {
                    lobbyName = ""
                    inLobby = false
                    if (channelId == LOBBY_CHANNEL_ID) {
                        setChannel(DEFAULT_CHANNEL_ID)
                    } else {
                        loadChannels()
                    }
                }
            } else if (type == "leave") {
                if (user == username) {
                    lobbyName = ""
                    inLobby = false
                    if (channelId == LOBBY_CHANNEL_ID) {
                        setChannel(DEFAULT_CHANNEL_ID)
                    }
                    loadChannels()
                }
            }
        }

        channelAddedListener = Communication.getChannelAddedListener().subscribe{
            loadChannels()
        }

        channelRemovedListener = Communication.getChannelRemovedListener().subscribe{
            activity!!.runOnUiThread {
                var channelToRemove: Item<ViewHolder>? = null
                for ( view in 0 until notSubChannelAdapter.itemCount) {
                    if (notSubChannelAdapter.getItem(view).toString() == it) {
                        channelToRemove = notSubChannelAdapter.getItem(view)
                        break
                    }
                }
                if (channelToRemove != null) {
                    notSubChannelAdapter.remove(channelToRemove)
                }
            }
        }

        v.recyclerView_chat_log.adapter = messageAdapter

        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        chatListener.dispose()
        channelAddedListener.dispose()
        channelRemovedListener.dispose()
        startGameSub.dispose()
        gameChatSub.dispose()
        lobbyChatSub.dispose()
        lobbyNotifSub.dispose()
        endGameSub.dispose()
    }

    private fun addGameChannel() {
        activity!!.runOnUiThread {
            channelAdapter.add(ChannelItem(GAME_CHANNEL_ID, true, controller, this))
        }
    }

    private fun addLobbyChannel() {
        activity!!.runOnUiThread {
            channelAdapter.add(ChannelItem(LOBBY_CHANNEL_ID, true, controller, this))
        }
    }

    private fun sendInput(v: View) {
        if (v.chat_message_editText.text.trim().isNotEmpty()) {
            when (channelId) {
                GAME_CHANNEL_ID -> {
                    val obj = buildGameplayMessage(username, v.chat_message_editText)
                    SocketIO.sendMessage("gameplay", obj)
                }
                LOBBY_CHANNEL_ID -> {
                    val obj = buildLobbyMessage(username, v.chat_message_editText)
                    SocketIO.sendMessage("lobby-chat", obj)
                }
                else -> {
                    val message = buildMessage(username, v.chat_message_editText, channelId)
                    SocketIO.sendMessage("chat", message)
                }
            }

            v.chat_message_editText.text.clear()
        }
    }

    private fun onButtonShowPopupWindowClick(inflater: LayoutInflater) {
        val popupView = inflater.inflate(R.layout.popup_create_channel, null)
        val width = LinearLayout.LayoutParams.WRAP_CONTENT
        val height = LinearLayout.LayoutParams.WRAP_CONTENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        activity!!.runOnUiThread {
            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0)

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
    }

    fun showLoadHistoryButton() {
        if (activity != null) {
            activity!!.runOnUiThread {
                v.button_load_chat_history.isEnabled = true
                v.button_load_chat_history.visibility = View.VISIBLE
            }
        }
    }

    fun hideLoadHistoryButton() {
        activity!!.runOnUiThread {
            v.button_load_chat_history.isEnabled = false
            v.button_load_chat_history.visibility = View.GONE
        }
    }

    fun loadChannels(search: String? = null){
        activity!!.runOnUiThread {
            controller.loadChannels(this, search)
        }
    }

    fun setChannel(newChannelId: String) {
        if (newChannelId == LOBBY_CHANNEL_ID && !inLobby)
            return

        if (newChannelId == GAME_CHANNEL_ID && !inGame)
            return

        activity!!.runOnUiThread {
            if (newChannelId != channelId) {
                loadChannels()
                messageAdapter.clear()
                hideLoadHistoryButton()
                val route =
                    when (newChannelId) {
                        LOBBY_CHANNEL_ID -> "/game/lobby/messages/$lobbyName"
                        GAME_CHANNEL_ID -> "/game/arena/messages/$username"
                        else -> "/chat/messages/$newChannelId"
                    }

                controller.showLoadHistoryButtonIfPreviousMessages(this, route)

                channelId = newChannelId
                textViewChannelName.text = channelId
            }
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

    private fun buildLobbyMessage(username: String, message: EditText): JSONObject {
        val obj = JSONObject()
        obj.put("lobbyName", lobbyName)
        obj.put("username", username)
        obj.put("content", message.text.toString().trim())

        return obj
    }

    private fun receiveGameMessage(mes: JSONObject) {
        val user = if (mes.isNull("username")) "" else mes.getString("username")
        val content = if (mes.isNull("content")) "" else mes.getString("content")
        val isServer = if (mes.isNull("isServer")) false else mes.getBoolean("isServer")

        activity!!.runOnUiThread {
            if (channelId == GAME_CHANNEL_ID) {
                when {
                    isServer -> {
                        messageAdapter.add(GameServerChatItemReceived(content))
                    }
                    user == username -> {
                        messageAdapter.add(GameChatItemSent(content))
                    }
                    else -> {
                        messageAdapter.add(GameChatItemReceived(content, user))
                    }
                }
            }
        }
    }

    private fun receiveLobbyMessage(mes: JSONObject) {
        val user = if (mes.isNull("username")) "" else mes.getString("username")
        val content = if (mes.isNull("content")) "" else mes.getString("content")
        val lobby = if (mes.isNull("lobbyName")) "" else mes.getString("lobbyName")

        activity!!.runOnUiThread {
            if (channelId == LOBBY_CHANNEL_ID && lobby == lobbyName) {
                if (user == username) {
                    messageAdapter.add(GameChatItemSent(content))
                } else {
                    messageAdapter.add(GameChatItemReceived(content, user))
                }
            }
        }
    }

    fun receiveMessages(
        adapter: GroupAdapter<ViewHolder>,
        curUser: String,
        messages: JSONArray,
        channel: String? = null,
        isNormalChannel: Boolean = true){
        for (i in 0 until messages.length()){
            val message = messages.getJSONObject(i)

            val id = if (message.isNull("channel_id")) "" else message.getString("channel_id")

            if (channel != null) {
                if (channel != channelId) {
                    continue
                }
            } else if (id != channelId) {
                continue
            }

            val username = if (message.isNull("username")) "" else message.getString("username")
            val content = if (message.isNull("content")) "" else message.getString("content")
            var time = ""
            if (isNormalChannel) {
                time = if (message.isNull("time")) "" else message.getString("time")
            }

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
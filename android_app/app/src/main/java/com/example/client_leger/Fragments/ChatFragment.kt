package com.example.client_leger.Fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.text.InputFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.example.client_leger.*
import com.example.client_leger.Communication.Communication
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_chat.view.*
import org.json.JSONObject

class ChatFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_chat, container, false)

        val adapter = GroupAdapter<ViewHolder>()
        val username = activity!!.intent.getStringExtra("username")

        SocketIO.connect(username)

        val fArray = arrayOfNulls<InputFilter>(1)
        fArray[0] = InputFilter.LengthFilter(Constants.MESSAGE_MAX_LENGTH)
        v.chat_message_editText.filters = fArray

        v.chat_message_editText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (v.chat_message_editText.text.trim().length > 0) {
                    val message = buildMessage(username, v.chat_message_editText, "general")
                    SocketIO.sendMessage("chat", message)
                }
                return@OnKeyListener true
            }
            false
        })

        v.chat_send_button.setOnClickListener {
            if (v.chat_message_editText.text.trim().length > 0) {
                val message = buildMessage(username, v.chat_message_editText, "general")
                SocketIO.sendMessage("chat", message)
            }
        }

        v.disconnect_button.setOnClickListener {
            SocketIO.disconnect()
            val intent = Intent(activity, LogPageActivity::class.java)
            startActivity(intent)
        }

        Communication.getChatMessageListener().subscribe{receptMes ->
            receiveMessage(adapter, v.recyclerView_chat_log, username, receptMes)
        }

        v.recyclerView_chat_log.adapter = adapter

        return v
    }

    private fun buildMessage(username: String, message: EditText, chan_id: String): JSONObject {
        val obj = JSONObject()
        obj.put("username", username)
        obj.put("channel_id", chan_id)
        obj.put("content", message.text.toString())

        message.text.clear()

        return obj
    }

    private fun receiveMessage(adapter: GroupAdapter<ViewHolder>, recyclerView: RecyclerView, curUser: String,  mes: JSONObject){

        val username = mes.getString("username")
        val content = mes.getString("content")
        val time = mes.getString("time")

        activity!!.runOnUiThread {
            if(curUser != username){
                adapter.add(ChatItemReceived(content, curUser, time))
            }
            else {
                adapter.add(ChatItemSent(content, time))
            }

            recyclerView.smoothScrollToPosition(adapter.itemCount)
        }
    }


}
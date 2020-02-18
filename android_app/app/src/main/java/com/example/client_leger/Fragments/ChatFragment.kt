package com.example.client_leger.Fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.InputFilter
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*

class ChatFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_chat, container, false)

        val adapter = GroupAdapter<ViewHolder>()
        val username = activity!!.intent.getStringExtra("username")

        val socket = SocketIO()
        socket.init(adapter, v.recyclerView_chat_log, activity!!)
        socket.connect(username)

        val fArray = arrayOfNulls<InputFilter>(1)
        fArray[0] = InputFilter.LengthFilter(Constants.MESSAGE_MAX_LENGTH)
        v.chat_message_editText.filters = fArray

        v.chat_message_editText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                if (v.chat_message_editText.text.trim().length > 0) {
                    socket.sendMessage(v.chat_message_editText, username)
                }
                return@OnKeyListener true
            }
            false
        })

        v.chat_send_button.setOnClickListener {
            if (v.chat_message_editText.text.trim().length > 0) {
                socket.sendMessage(v.chat_message_editText, username)
            }
        }

        v.disconnect_button.setOnClickListener {
            socket.disconnect()
            val intent = Intent(activity, LogPageActivity::class.java)
            startActivity(intent)
        }

        v.recyclerView_chat_log.adapter = adapter

        return v
    }
}
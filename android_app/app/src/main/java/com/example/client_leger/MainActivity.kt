package com.example.client_leger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_chat.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_chat)
        chat_message_editText.requestFocus()
        val adapter = GroupAdapter<ViewHolder>()
        val username = intent.getStringExtra("username")

        val socket = SocketIO()
        socket.init(adapter, recyclerView_chat_log, this)
        socket.connect(username)

        chat_message_editText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                socket.sendMessage(chat_message_editText, username)
                return@OnKeyListener true
            }
            false
        })

        chat_send_button.setOnClickListener {
            socket.sendMessage(chat_message_editText, username)
        }

        recyclerView_chat_log.adapter = adapter
    }
}



package com.example.client_leger

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_login.*

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
                if (chat_message_editText.text.trim().length > 0) {
                    socket.sendMessage(adapter, chat_message_editText, username, recyclerView_chat_log)
                }
                return@OnKeyListener true
            }
            false
        })

        chat_send_button.setOnClickListener {
            if (chat_message_editText.text.trim().length > 0) {
                socket.sendMessage(adapter, chat_message_editText, username, recyclerView_chat_log)
            }
        }

        disconect_button.setOnClickListener {
            socket.disconnect()
            val intent = Intent(this, LogPageActivity::class.java)
            startActivity(intent)
        }

        recyclerView_chat_log.adapter = adapter
    }
}



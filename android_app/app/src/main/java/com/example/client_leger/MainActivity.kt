package com.example.client_leger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.fragment_chat.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_chat)
        chat_message_editText.requestFocus()
        val adapter = GroupAdapter<ViewHolder>()
        val socket = IO.socket("http://10.200.29.191:3000/") //Your IPV4 here!!

        socket.connect()
        Log.w("socket","is connecting")

        socket.on(Socket.EVENT_CONNECT) {
          Log.w("socket","bitconneeeeeeeeeect")
        }

        socket.on(Socket.EVENT_MESSAGE) { arg0 ->
            Log.w("socket", "socket event message$arg0")
        }

        socket.emit("chat", "android to server from event message")

        chat_message_editText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                sendMessage(adapter, chat_message_editText, recyclerView_chat_log)
                return@OnKeyListener true
            }
            false
        })

        chat_send_button.setOnClickListener {
            sendMessage(adapter, chat_message_editText, recyclerView_chat_log)
        }

        recyclerView_chat_log.adapter = adapter
    }
}

fun sendMessage(adapter: GroupAdapter<ViewHolder>, textInput: EditText, recyclerView: RecyclerView){
    //TODO: really send message.
    adapter.add(ChatItemSent(textInput.text.toString()))
    textInput.text.clear()
    recyclerView.smoothScrollToPosition(adapter.itemCount)
}


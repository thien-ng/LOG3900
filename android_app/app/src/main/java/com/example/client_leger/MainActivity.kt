package com.example.client_leger

import android.app.Activity
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
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_chat)
        chat_message_editText.requestFocus()
        val adapter = GroupAdapter<ViewHolder>()
        val socket = IO.socket("http://72.53.102.93:3000/")

        socket.connect()

        socket.on(Socket.EVENT_CONNECT) {
          Log.w("socket","bitconneeeeeeeeeect")
        }

        socket.on(Socket.EVENT_MESSAGE) {
            val data = it[0] as JSONObject
            receiveMessage(adapter, data.getString("username"), data.getString("message"), recyclerView_chat_log, this)
        }

        chat_message_editText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                sendMessage(adapter, chat_message_editText, intent.getStringExtra("username"), recyclerView_chat_log, socket)
                return@OnKeyListener true
            }
            false
        })

        chat_send_button.setOnClickListener {
            sendMessage(adapter, chat_message_editText, intent.getStringExtra("username"), recyclerView_chat_log, socket)
        }

        recyclerView_chat_log.adapter = adapter
    }
}

fun sendMessage(adapter: GroupAdapter<ViewHolder>, textInput: EditText, username: String, recyclerView: RecyclerView, socket: Socket  ){
    adapter.add(ChatItemSent(textInput.text.toString()))
    recyclerView.smoothScrollToPosition(adapter.itemCount)
    val obj = JSONObject()
    obj.put("username", username)
    obj.put("message", textInput.text.toString())

    textInput.text.clear()
    socket.emit("message", obj)
}

fun receiveMessage(adapter: GroupAdapter<ViewHolder>, username: String, message: String, recyclerView: RecyclerView, activity: Activity){
    activity.runOnUiThread {
        if(username != activity.intent.getStringExtra("username")){
            adapter.add(ChatItemReceived(message, username))
            recyclerView.smoothScrollToPosition(adapter.itemCount)
        }
    }
}

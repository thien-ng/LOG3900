package com.example.client_leger

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.EditText
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.fragment_chat.*
import org.json.JSONObject

class SocketIO(){
    lateinit var socket: Socket

    fun init(adapter: GroupAdapter<ViewHolder>, recyclerView: RecyclerView, activity: Activity) {
        socket = IO.socket("http://72.53.102.93:3000/")

        socket.on(Socket.EVENT_CONNECT) {
            Log.w("socket","Connected to server!")
        }

        socket.on(Socket.EVENT_MESSAGE) {
            val data = it[0] as JSONObject
            receiveMessage(adapter, data.getString("username"), data.getString("message"), recyclerView, activity)
        }
    }

    fun connect() {
        socket.connect()
    }

    fun sendMessage(adapter: GroupAdapter<ViewHolder>, textInput: EditText, username: String, recyclerView: RecyclerView){
        adapter.add(ChatItemSent(textInput.text.toString()))
        recyclerView.smoothScrollToPosition(adapter.itemCount)
        val obj = JSONObject()
        obj.put("username", username)
        obj.put("message", textInput.text.toString())

        textInput.text.clear()
        socket.emit("message", obj)
    }

    private fun receiveMessage(adapter: GroupAdapter<ViewHolder>, username: String, message: String, recyclerView: RecyclerView, activity: Activity){
        activity.runOnUiThread {
            if(username != activity.intent.getStringExtra("username")){
                adapter.add(ChatItemReceived(message, username))
                recyclerView.smoothScrollToPosition(adapter.itemCount)
            }
        }
    }
}
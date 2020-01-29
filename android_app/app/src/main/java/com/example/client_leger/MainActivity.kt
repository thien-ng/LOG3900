package com.example.client_leger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.fragment_chat.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_chat)
        chat_message_editText.requestFocus()

        Log.w("socket", "BEFORE SOCKET APOCALYPSE")

        val adapter = GroupAdapter<ViewHolder>()

        val socket = IO.socket("http://10.200.21.11:3000")

        Log.w("socket", "after connect")

        socket.on(Socket.EVENT_CONNECT) {
          Log.w("","===================connect")
        }
        socket.connect()
//
//        val thread = Thread(Runnable {
//            try {
//                val socket = Socket("10.200.21.11", 3333)
//                val osw = OutputStreamWriter(socket.getOutputStream(), "UTF-8")
//                val isr = InputStreamReader(socket.getInputStream())
//                val str = "testHenlo"
//                osw.write(str, 0, str.length)
//                var testStr = isr.readText()
//                adapter.add(ChatItemSent(testStr))
//                Log.w("socket", testStr)
//                Log.w("socket", "AAAAAAAAAAAAAAAAAAAAAAAAAAAA")
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        })
//
//        thread.start()
//        thread.run()
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


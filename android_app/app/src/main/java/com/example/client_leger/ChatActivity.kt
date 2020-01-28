package com.example.client_leger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.fragment_chat.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.my_message_layout.view.*
import kotlinx.android.synthetic.main.their_message_layout.view.*

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_chat)
        chat_message_editText.requestFocus()

        val adapter = GroupAdapter<ViewHolder>()

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

class ChatItemReceived(val text: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int){
        viewHolder.itemView.their_message_body.text = text
    }

    override fun getLayout(): Int {
        return R.layout.their_message_layout
    }
}

class ChatItemSent(val text: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int){
        viewHolder.itemView.my_message_body.text = text
    }

    override fun getLayout(): Int {
        return R.layout.my_message_layout
    }
}
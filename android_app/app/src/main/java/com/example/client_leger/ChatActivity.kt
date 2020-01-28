package com.example.client_leger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

        val adapter = GroupAdapter<ViewHolder>()

        chat_send_button.setOnClickListener {
            //TODO: really send message.
            adapter.add(ChatItemSent(chat_message_editText.text.toString()))
        }

        recyclerView_chat_log.adapter = adapter
    }
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
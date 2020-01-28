package com.example.client_leger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_chat.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder

class ChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_chat)

        val adapter = GroupAdapter<ViewHolder>()
        recyclerView_chat_log.adapter = adapter
    }
}

class ChatItemReceived: Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int){

    }

    override fun getLayout(): Int {
        return R.layout.their_message_layout
    }
}

class ChatItemSent: Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int){

    }

    override fun getLayout(): Int {
        return R.layout.my_message_layout
    }
}
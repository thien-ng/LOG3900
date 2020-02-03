package com.example.client_leger

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.my_message_layout.view.*
import kotlinx.android.synthetic.main.their_message_layout.view.*

class ChatItemReceived(val message: String, val username: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int){
        viewHolder.itemView.their_message_body.text = message
        viewHolder.itemView.name.text = username
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
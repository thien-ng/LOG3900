package com.example.client_leger

import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_chat.view.textView_channelName

class ChannelItem(private val channelId: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int){
        viewHolder.itemView.textView_channelName.text = channelId
    }

    override fun getLayout(): Int {
        return R.layout.channel_layout
    }

    override fun toString(): String {
        return channelId
    }
}

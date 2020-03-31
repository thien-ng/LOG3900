package com.example.client_leger

import android.view.View
import com.example.client_leger.Fragments.ChatFragment
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.channel_layout.view.*
import kotlinx.android.synthetic.main.fragment_chat.view.textView_channelName


class ChannelItem(private val channelId: String, private val isSub: Boolean, private val controller: ConnexionController, private val activity: ChatFragment): Item<ViewHolder>(){

    override fun bind(viewHolder: ViewHolder, position: Int){
        viewHolder.itemView.textView_channelName.text = channelId
        if (isSub && channelId != Constants.DEFAULT_CHANNEL_ID && channelId != "") {
            viewHolder.itemView.imageButton_leaveChannel.setOnClickListener {
                controller.leaveChannel(activity, channelId)
            }
        }
        else {
            viewHolder.itemView.imageButton_leaveChannel.visibility = View.GONE
        }

        var string = if (channelId.isNotEmpty()) {
            channelId.substring(0, 1).toUpperCase()
        } else {
            "G"
        }
        if (channelId.length >= 2) {
            string += channelId.substring(1, 2)
        }
        viewHolder.itemView.channel_image.text = string

    }

    override fun getLayout(): Int {
        return R.layout.channel_layout
    }

    override fun toString(): String {
        return channelId
    }
}

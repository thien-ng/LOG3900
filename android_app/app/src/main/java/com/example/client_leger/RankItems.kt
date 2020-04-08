package com.example.client_leger

import android.util.Log
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.user_ranking_item.view.*

class RankItem(private val rank: Int, val username: String, private val points: Int): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int){
        Log.w("rank", "rank: $rank")
        Log.w("rank", "username: $username")
        Log.w("rank", "points: $points")
        viewHolder.itemView.textView_rank.text = rank.toString()
        viewHolder.itemView.textView_username.text = username
        viewHolder.itemView.textView_points.text = points.toString()
    }

    override fun getLayout(): Int {
        return R.layout.user_ranking_item
    }
}

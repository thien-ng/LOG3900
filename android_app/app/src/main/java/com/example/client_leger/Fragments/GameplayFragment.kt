package com.example.client_leger.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import com.example.client_leger.Communication.Communication
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.R
import com.example.client_leger.RankItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_chat.view.*
import kotlinx.android.synthetic.main.popup_end_game.view.*

class GameplayFragment: Fragment(), FragmentChangeListener {

    private lateinit var endGameSub: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_gameplay, container, false)

        fragmentManager!!.beginTransaction()
            .replace(R.id.container_view_top, GameplayMenuFragment(), "menu")
            .replace(R.id.container_view_canvas, DrawFragment(), "draw")
            .commit()

        endGameSub = Communication.getEndGameListener().subscribe{ res ->
            activity!!.runOnUiThread {
                val rankingView = layoutInflater.inflate(R.layout.popup_end_game, null)
                val popupWindow = PopupWindow(
                    rankingView,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    false
                )

                val rankAdapter = GroupAdapter<ViewHolder>()
                rankingView.recyclerView_ranks.adapter = rankAdapter

                val ranks = res.getJSONArray("points")
                Log.w("rank", "ranks.length(): " + ranks.length())

                for (i in 0 until ranks.length()) {
                    Log.w("rank", i.toString())
                    val infos = ranks.getJSONObject(i)
                    Log.w("rank", "ranks.getJSONObject(i): " + ranks.getJSONObject(i).toString())
                    Log.w("rank", "infos.getString(\"username\"): " + infos.getString("username"))
                    Log.w("rank", "infos.getInt(\"points\"): " + infos.getInt("points"))
                    rankAdapter.add(RankItem(
                        i + 1,
                        infos.getString("username"),
                        infos.getInt("points")
                    ))
                }

                popupWindow.showAtLocation(rankingView, Gravity.CENTER, 0, 0)

                rankingView.button_ok.setOnClickListener {
                    replaceFragment(LobbyCardsFragment())
                    popupWindow.dismiss()
                }
            }
        }

        return v
    }

    override fun replaceFragment(fragment: Fragment) {
        val menu = fragmentManager!!.findFragmentByTag("menu")
        val draw = fragmentManager!!.findFragmentByTag("draw")
        if (menu != null && draw != null) {
            fragmentManager!!.beginTransaction()
                .remove(menu)
                .remove(draw)
                .replace(R.id.container_view_right, fragment).commit()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        endGameSub.dispose()
    }
}
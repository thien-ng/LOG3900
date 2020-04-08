package com.example.client_leger.Fragments

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.PopupWindow
import android.widget.TableRow
import android.widget.TextView
import com.example.client_leger.Communication.Communication
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.R
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.popup_end_game.view.*
import java.util.*


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

                var tableRow = TableRow(this.context)
                tableRow.layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )

                val title1 = TextView(this.context)
                title1.text = "Rank"
                title1.textSize = 20F
                tableRow.addView(title1)

                val title2 = TextView(this.context)
                title2.setPadding(120 - title1.width, 0, 0, 0)
                title2.textSize = 20F
                title2.text = "Username"
                tableRow.addView(title2)

                val title3 = TextView(this.context)
                title3.setPadding(110, 0, 0, 0)
                title3.text = "Points"
                title3.textSize = 20F
                tableRow.addView(title3)

                rankingView.tableView_ranks.addView(tableRow)

                val ranks = res.getJSONArray("points")

                for (i in 0 until ranks.length()) {
                    Log.w("rank", i.toString())
                    val infos = ranks.getJSONObject(i)

                    tableRow = TableRow(this.context)

                    val rnd = Random()
                    val color = Color.argb(
                        255,
                        rnd.nextInt(256),
                        rnd.nextInt(256),
                        rnd.nextInt(256)
                    )
                    tableRow.setBackgroundColor(color)

                    tableRow.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )

                    val b = TextView(this.context)
                    b.setPadding(10, 0, 0, 0)
                    val str: String = java.lang.String.valueOf(i + 1)
                    b.text = str
                    b.setTextColor(Color.BLACK)
                    b.textSize = 20F
                    tableRow.addView(b)

                    val b1 = TextView(this.context)
                    b1.setPadding(180 - b.width, 0, 0, 0)
                    b1.textSize = 20F
                    val str1: String = infos.getString("username")
                    b1.text = str1
                    b1.setTextColor(Color.BLACK)
                    tableRow.addView(b1)

                    val b2 = TextView(this.context)
                    b2.setPadding(140 - str1.length, 0, 0, 0)
                    val str2: String = infos.getInt("points").toString()
                    b2.text = str2
                    b2.setTextColor(Color.BLACK)
                    b2.textSize = 20F
                    tableRow.addView(b2)

                    rankingView.tableView_ranks.addView(tableRow)
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
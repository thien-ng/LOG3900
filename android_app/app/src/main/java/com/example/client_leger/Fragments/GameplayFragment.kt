package com.example.client_leger.Fragments

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.PopupWindow
import android.widget.TableRow
import android.widget.TextView
import com.example.client_leger.Communication.Communication
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.R
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.popup_end_game.view.*


class GameplayFragment: Fragment(), FragmentChangeListener {

    private lateinit var endGameSub: Disposable
    private var colorList = arrayListOf("#FF90EE90", "#FFFFA500", "#FFFF0000", "#FFD3D3D3")

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
                    true
                )

                popupWindow.setOnDismissListener {
                    replaceFragment(LobbyCardsFragment())
                }

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
                    val infos = ranks.getJSONObject(i)

                    tableRow = TableRow(this.context)

                    if (i >= 4) {
                        tableRow.setBackgroundColor(Color.parseColor(colorList[3]))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor(colorList[i]))
                    }

                    tableRow.layoutParams = TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT
                    )

                    val rank = TextView(this.context)
                    rank.setPadding(10, 0, 0, 0)
                    val str: String = java.lang.String.valueOf(i + 1)
                    rank.text = str
                    rank.setTextColor(Color.BLACK)
                    rank.textSize = 20F
                    tableRow.addView(rank)

                    val user = TextView(this.context)
                    user.setPadding(180 - rank.width, 0, 0, 0)
                    user.textSize = 20F
                    val str1: String = infos.getString("username")
                    user.text = str1
                    user.setTextColor(Color.BLACK)
                    tableRow.addView(user)

                    val points = TextView(this.context)
                    points.setPadding(140 - str1.length, 0, 0, 0)
                    val str2: String = infos.getInt("points").toString()
                    points.text = str2
                    points.setTextColor(Color.BLACK)
                    points.textSize = 20F
                    tableRow.addView(points)

                    rankingView.tableView_ranks.addView(tableRow)
                }

                popupWindow.showAtLocation(rankingView, Gravity.CENTER, 0, 0)

                rankingView.button_ok.setOnClickListener {
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
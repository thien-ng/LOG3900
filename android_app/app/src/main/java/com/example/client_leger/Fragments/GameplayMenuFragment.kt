package com.example.client_leger.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.Communication.Communication
import com.example.client_leger.R
import kotlinx.android.synthetic.main.fragment_gameplay_menu.view.*

class GameplayMenuFragment: Fragment() {

    lateinit var username: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_gameplay_menu, container, false)

        username = activity!!.intent.getStringExtra("username")

        Communication.getTimerListener().subscribe{res ->
            Log.w("TEDAWD", res.toString())

            activity!!.runOnUiThread {
                v.timer.setText(res.getString("time"))
            }
        }

        Communication.getDrawerUpdateListener().subscribe{res ->
            activity!!.runOnUiThread {
                if (res.getString("username") == username)
                    v.role.setText("drawer")
                else
                    v.role.setText("guesser")
            }
        }

        return v
    }
}
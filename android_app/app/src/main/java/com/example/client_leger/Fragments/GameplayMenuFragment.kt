package com.example.client_leger.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.Communication.Communication
import com.example.client_leger.R
import com.example.client_leger.SocketIO
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_gameplay_menu.view.*
import org.json.JSONObject

class GameplayMenuFragment: Fragment() {

    lateinit var username:  String

    private lateinit var timerSub:  Disposable
    private lateinit var drawerSub: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_gameplay_menu, container, false)

        username = activity!!.intent.getStringExtra("username")

        // Emit to tell server the view is ready
        val readyState = JSONObject()
        readyState.put("event", "ready")
        readyState.put("username", username)
        SocketIO.sendMessage("gameplay", readyState)

        timerSub = Communication.getTimerListener().subscribe{res ->
            activity!!.runOnUiThread {
                v.timer.text = res.getString("time")
            }
        }

        drawerSub = Communication.getDrawerUpdateListener().subscribe{res ->
            activity!!.runOnUiThread {
                if (res.getString("username") == username) {
                    v.role.text = "drawer"
                    v.item.text = res.getString("object")
                } else {
                    v.role.text = "guesser"
                    v.item.text = ""
                }
            }
        }

        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        timerSub.dispose()
        drawerSub.dispose()
    }
}
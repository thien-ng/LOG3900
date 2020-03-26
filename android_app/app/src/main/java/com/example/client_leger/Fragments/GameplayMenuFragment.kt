package com.example.client_leger.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.Communication.Communication
import com.example.client_leger.R
import com.example.client_leger.SocketIO
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_gameplay_menu.view.*
import org.json.JSONObject

class GameplayMenuFragment: Fragment() {

    lateinit var username:  String

    lateinit var timerSub:  Disposable;
    lateinit var drawerSub: Disposable;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_gameplay_menu, container, false)

        username = activity!!.intent.getStringExtra("username")

        // Emit to tell server the view is ready
        SocketIO.sendMessage("gameplay", JSONObject().put("username", username))

        timerSub = Communication.getTimerListener().subscribe{res ->
            activity!!.runOnUiThread {
                v.timer.setText(res.getString("time"))
            }
        }

        drawerSub = Communication.getDrawerUpdateListener().subscribe{res ->
            activity!!.runOnUiThread {
                if (res.getString("username") == username)
                    v.role.setText("drawer")
                else
                    v.role.setText("guesser")
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
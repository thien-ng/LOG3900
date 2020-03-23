package com.example.client_leger.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.Communication.Communication
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.R

class GameplayFragment: Fragment(), FragmentChangeListener {

    private var menu = GameplayMenuFragment()
    private var draw = DrawFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_gameplay, container, false)

        fragmentManager!!.beginTransaction().replace(R.id.container_view_top, menu).commit()
        fragmentManager!!.beginTransaction().replace(R.id.container_view_bottom, draw).commit()

        Communication.getEndGameListener().subscribe{ res ->
            // TODO show points from res, and then change to lobbyCardsFragment
            Log.w("Points", res.toString())
            replaceFragment(LobbyCardsFragment())
        }

        return v
    }

    override fun replaceFragment(fragment: Fragment) {
        fragmentManager!!.beginTransaction()
            .remove(menu)
            .remove(draw)
            .replace(R.id.container_view_right, fragment).commit()
    }

}
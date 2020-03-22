package com.example.client_leger.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.R

class GameplayFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_gameplay, container, false)

        fragmentManager!!.beginTransaction().replace(R.id.container_view_top, GameplayMenuFragment()).commit()
        fragmentManager!!.beginTransaction().replace(R.id.container_view_bottom, DrawFragment()).commit()

        return v
    }

}
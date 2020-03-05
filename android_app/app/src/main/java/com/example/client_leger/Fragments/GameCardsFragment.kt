package com.example.client_leger.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.Adapters.GameCardRecyclerViewAdapter
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.R


class GameCardsFragment : Fragment(), GameCardRecyclerViewAdapter.ItemClickListener, FragmentChangeListener {
    private lateinit var adapter: GameCardRecyclerViewAdapter;
    private lateinit var recyclerViewGameCards: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_gamecards, container, false)
        val data = arrayOf("My Game", "TOTO", "apple apple", "impossible", "level 100", "testing", "game 3", "88888", "9", "final")
         recyclerViewGameCards = v.findViewById(R.id.recyclerView_gameCards)
        var numberOfColumns = 2
        recyclerViewGameCards.layoutManager = GridLayoutManager(context, numberOfColumns)
        adapter = GameCardRecyclerViewAdapter(context, data)
        adapter.setClickListener(this)
        recyclerViewGameCards.adapter = adapter
        return v
    }

    override fun onItemClick(view: View?, position: Int) {
        replaceFragment(DrawFragment())
    }

    override fun replaceFragment(fragment: Fragment) {
        fragmentManager!!.beginTransaction().replace(R.id.container_view_right, fragment).addToBackStack(fragment.toString()).commit()
    }
}
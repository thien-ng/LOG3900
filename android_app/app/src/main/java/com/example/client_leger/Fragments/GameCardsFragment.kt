package com.example.client_leger.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.Adapters.GameCardRecyclerViewAdapter
import com.example.client_leger.ConnexionController
import com.example.client_leger.Controller.GameCardsController
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.R
import org.json.JSONArray
import org.json.JSONObject


class GameCardsFragment : Fragment(), GameCardRecyclerViewAdapter.ItemClickListener, FragmentChangeListener {
    lateinit var username: String
    lateinit var adapter: GameCardRecyclerViewAdapter
    private lateinit var recyclerViewGameCards: RecyclerView
    private lateinit var gameCardsController: GameCardsController
    private lateinit var connexionController:ConnexionController

    private lateinit var gameCards: JSONArray

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_gamecards, container, false)
        username = activity!!.intent.getStringExtra("username")
        gameCardsController = GameCardsController()
        connexionController = ConnexionController()
        gameCards = JSONArray()
        gameCardsController.getGameCards(this)
        recyclerViewGameCards = v.findViewById(R.id.recyclerView_gameCards)
        var numberOfColumns = 2
        recyclerViewGameCards.layoutManager = GridLayoutManager(context, numberOfColumns)
        adapter = GameCardRecyclerViewAdapter(context, gameCards)
        adapter.setClickListener(this)
        recyclerViewGameCards.adapter = adapter
        return v
    }

    override fun onItemClick(view: View?, position: Int) {
        var lobby = JSONObject()
        lobby.put("username", username)
        lobby.put("private", false)
        lobby.put("lobbyName", "lobby")
        lobby.put("size",2)
        lobby.put("gameID", adapter.getItem(position).getString("gameID"))
        connexionController.joinLobby(this, lobby)
    }

    override fun replaceFragment(fragment: Fragment) {
        fragmentManager!!.beginTransaction().replace(R.id.container_view_right, fragment).addToBackStack(fragment.toString()).commit()
    }

    fun loadGameCards(adapter: GameCardRecyclerViewAdapter, gameCards: JSONArray) {
        adapter.addItems(gameCards)
    }
}
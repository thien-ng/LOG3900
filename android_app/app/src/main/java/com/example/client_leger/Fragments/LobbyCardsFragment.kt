package com.example.client_leger.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.client_leger.Adapters.LobbyCardRecyclerViewAdapter
import com.example.client_leger.ConnexionController
import com.example.client_leger.Controller.LobbyCardsController
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.R
import com.example.client_leger.models.Lobby
import org.json.JSONObject


class LobbyCardsFragment : Fragment(), LobbyCardRecyclerViewAdapter.ItemClickListener,
    FragmentChangeListener {
    lateinit var username: String
    lateinit var userListAdapter: ArrayAdapter<String>
    private lateinit var adapterLobbyCard: LobbyCardRecyclerViewAdapter
    private lateinit var recyclerViewGameCards: RecyclerView
    private lateinit var lobbyCardsController: LobbyCardsController
    private lateinit var connexionController: ConnexionController
    private lateinit var lobbyCards: ArrayList<Lobby>
    private lateinit var userList: ArrayList<String>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_gamecards, container, false)
        username = activity!!.intent.getStringExtra("username")
        lobbyCardsController = LobbyCardsController()
        connexionController = ConnexionController()
        lobbyCards = ArrayList()
        userList = ArrayList()
        userListAdapter = ArrayAdapter<String>(
            context,
            android.R.layout.simple_list_item_1,
            userList
        )
        lobbyCardsController.getLobbies(this, "FFA")
        recyclerViewGameCards = v.findViewById(R.id.recyclerView_gameCards)
        var numberOfColumns = 2
        recyclerViewGameCards.layoutManager = GridLayoutManager(context, numberOfColumns)
        adapterLobbyCard = LobbyCardRecyclerViewAdapter(context, lobbyCards)
        adapterLobbyCard.setClickListener(this)
        recyclerViewGameCards.adapter = adapterLobbyCard

        return v
    }

    override fun onItemClick(view: View?, position: Int) {

    }

    override fun onJoinClick(view: View?, position: Int) {
        var lobby = JSONObject()
        lobby.put("username", username)
        lobby.put("lobbyName", adapterLobbyCard.getItem(position).lobbyName)
        lobbyCardsController.joinLobby(this, lobby)
    }

    override fun onUsersDropClick(view: View?, position: Int) {
        if (view != null) {
            Log.d("FragmentOnClick", "toggleView")

            toggleView(view)
        }
    }

    override fun replaceFragment(fragment: Fragment) {
        fragmentManager!!.beginTransaction().replace(R.id.container_view_right, fragment)
            .addToBackStack(fragment.toString()).commit()
    }

    fun loadLobbies(lobbies: ArrayList<Lobby>) {
        adapterLobbyCard.addItems(lobbies)
    }

    private fun toggleView(v: View) {
        v.visibility = if (v.isShown) View.GONE else View.VISIBLE
    }
}
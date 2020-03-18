package com.example.client_leger.Fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.client_leger.Adapters.GameCardRecyclerViewAdapter
import com.example.client_leger.ConnexionController
import com.example.client_leger.Controller.GameCardsController
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.models.GameCard
import com.example.client_leger.models.Lobby
import com.example.client_leger.R
import org.json.JSONArray


class GameCardsFragment : Fragment(), GameCardRecyclerViewAdapter.ItemClickListener, FragmentChangeListener {
    lateinit var username: String
    lateinit var adapterGameCard: GameCardRecyclerViewAdapter
    private lateinit var recyclerViewGameCards: RecyclerView
    private lateinit var spinnerGameModes: Spinner
    private lateinit var gameCardsController: GameCardsController
    private lateinit var connexionController:ConnexionController
    private lateinit var gameCards: ArrayList<GameCard>

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_gamecards, container, false)
        username = activity!!.intent.getStringExtra("username")
        gameCardsController = GameCardsController()
        connexionController = ConnexionController()
        gameCards = ArrayList()
        gameCardsController.getGameCards(this)
        recyclerViewGameCards = v.findViewById(R.id.recyclerView_gameCards)
        var numberOfColumns = 2
        recyclerViewGameCards.layoutManager = GridLayoutManager(context, numberOfColumns)
        adapterGameCard = GameCardRecyclerViewAdapter(context, gameCards)
        adapterGameCard.setClickListener(this)
        recyclerViewGameCards.adapter = adapterGameCard
        spinnerGameModes = v.findViewById(R.id.GameMode)
        var gamemodes = arrayListOf<String>("Free for all","Sprint Solo","Sprint Co-op")
        var dataAdapter  = ArrayAdapter<String>(context,  R.layout.gamemode_item, gamemodes)
        spinnerGameModes.adapter = dataAdapter
        return v
    }

    override fun onItemClick(view: View?, position: Int) {
        gameCardsController.getLobbies(
            this,
            adapterGameCard.getItem(position).gameId
        )
    }

    override fun replaceFragment(fragment: Fragment) {
        fragmentManager!!.beginTransaction().replace(R.id.container_view_right, fragment).addToBackStack(fragment.toString()).commit()
    }

    fun loadGameCards(gameCards: JSONArray) {
        adapterGameCard.addItems(responseToGameCards(gameCards, this!!.context!!))
    }

    fun loadLobbies(lobbies:ArrayList<Lobby>, gameID:String){
        adapterGameCard.addLobbies(lobbies,gameID)
    }

    private fun responseToGameCards(response: JSONArray, context: Context): ArrayList<GameCard>{
        var gameCards = arrayListOf<GameCard>()
        for(i in 0 until response.length()){
            gameCards.add(GameCard(response.getJSONObject(i), context))
        }
        return gameCards
    }
}
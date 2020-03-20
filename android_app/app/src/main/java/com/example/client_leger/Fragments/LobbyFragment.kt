package com.example.client_leger.Fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.client_leger.Controller.GameController
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.R
import org.json.JSONArray


class LobbyFragment : Fragment(),
    FragmentChangeListener {
    private var gameController: GameController = GameController()
    private lateinit var username: String
    private lateinit var lobbyName:String
    private var usernames: ArrayList<String> = arrayListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_lobby, container, false)
        username = activity!!.intent.getStringExtra("username")
        val bundle = this.arguments
        lobbyName = ""
        if (bundle != null) {
            lobbyName = bundle.getString("lobbyName")
        }
        gameController.getUsers(this, lobbyName)


        return  v
    }

    private fun startGame(lobbyName: String){
        gameController.startGame(this, lobbyName)
    }

    override fun replaceFragment(fragment: Fragment) {
        fragmentManager!!.beginTransaction().replace(R.id.container_view_right, fragment)
            .addToBackStack(fragment.toString()).commit()
    }

    fun loadUsers(userJsonArray: JSONArray) {
        for (i in 0 until userJsonArray.length()){
            usernames.add(userJsonArray.get(i).toString())
        }
        if(usernames.isNotEmpty()) {
            var startButton = view!!.findViewById<Button>(R.id.button_start)
            if(usernames[0] == username) {
                startButton.visibility = View.VISIBLE
                startButton.isEnabled = true
                startButton.setOnClickListener { startGame(lobbyName) }
            }
        }
    }
}
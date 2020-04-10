package com.example.client_leger.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import com.example.client_leger.Adapters.UserListViewAdapter
import com.example.client_leger.Communication.Communication
import com.example.client_leger.Controller.GameController
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.R
import io.reactivex.rxjava3.disposables.Disposable
import org.json.JSONArray
import org.json.JSONObject


class LobbyFragment : Fragment(),
    FragmentChangeListener {
    private var gameController: GameController = GameController()
    private lateinit var username: String
    private lateinit var lobbyName: String
    private var usernames: ArrayList<String> = arrayListOf()
    private var numOtherPlayers = 0
    private lateinit var v: View
    private lateinit var userListAdapter: UserListViewAdapter
    private lateinit var startListener: Disposable
    private lateinit var lobbyNotifSub: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_lobby, container, false)
        username = activity!!.intent.getStringExtra("username")
        val bundle = this.arguments
        lobbyName = ""
        var mode = ""
        if (bundle != null) {
            lobbyName = bundle.getString("lobbyName")!!
            mode = bundle.getString("mode")!!
        }
        gameController.getUsers(this, lobbyName, mode)

        userListAdapter = UserListViewAdapter(this)
        val listview = v.findViewById<ListView>(R.id.userlist)
        listview.adapter = userListAdapter



        startListener = Communication.getGameStartListener().subscribe {
            activity!!.runOnUiThread {
                replaceFragment(GameplayFragment())
            }
        }

        lobbyNotifSub = Communication.getLobbyUpdateListener().subscribe { mes ->
            when (mes.getString("type")) {
                "join" -> {
                    val user = mes.getString("username")

                    if (username != user) {
                        ++numOtherPlayers
                        activity!!.runOnUiThread {
                            val startButton = v.findViewById<Button>(R.id.button_start)
                            startButton.visibility = View.VISIBLE
                            startButton.isEnabled = true
                            startButton.setOnClickListener { startGame(lobbyName) }
                            if(user.startsWith("bot:")){
                                userListAdapter.addBot(user)
                            } else userListAdapter.addUser(user)
                        }
                    }
                }
                "leave" -> {
                    val user = mes.getString("username")
                    if(user != username) {
                        --numOtherPlayers
                        activity!!.runOnUiThread {
                            if (numOtherPlayers == 0) {
                                val startButton = v.findViewById<Button>(R.id.button_start)
                                startButton.visibility = View.GONE
                                startButton.isEnabled = false
                            }
                            if(user.startsWith("bot:")){
                                userListAdapter.removeUser(user)
                            } else userListAdapter.removeUser(user)
                        }
                    }
                }
            }
        }
        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        startListener.dispose()
        lobbyNotifSub.dispose()
    }

    private fun startGame(lobbyName: String) {
        gameController.startGame(this, lobbyName)
    }

    private fun leaveGame(lobbyName: String) {
        val body = JSONObject()
        body.put("lobbyName", lobbyName)
        body.put("username", username)
        gameController.leaveGame(this, body)
    }

    override fun replaceFragment(fragment: Fragment) {
        fragmentManager!!.beginTransaction().replace(R.id.container_view_right, fragment)
            .addToBackStack(fragment.toString()).commit()
    }

    fun loadUsers(userJsonArray: JSONArray, mode: String) {
        activity!!.runOnUiThread {
            for (i in 0 until userJsonArray.length()) {
                if (userJsonArray.get(i).toString().startsWith("bot:")) {
                    userListAdapter.addBot(userJsonArray.get(i).toString())
                } else usernames.add(userJsonArray.get(i).toString())
            }
            if (usernames.isNotEmpty()) {
                val startButton = v.findViewById<Button>(R.id.button_start)
                val leaveButton = v.findViewById<Button>(R.id.button_leave)
                val addBotButton = v.findViewById<Button>(R.id.button_addBot)
                if (usernames[0] == username) {
                    if (mode == "FFA") {
                        addBotButton.visibility = View.VISIBLE
                        addBotButton.isEnabled = true
                        addBotButton.setOnClickListener {
                            val builder = AlertDialog.Builder(context)
                            val bots = arrayOf("bot:olivier", "bot:sebastien", "bot:olivia")
                            builder.setTitle("Select your bot")
                            builder.setSingleChoiceItems(bots, -1) { dialogInterface, i ->
                                addBot(bots[i])
                                dialogInterface.dismiss()
                            }
                            builder.setNeutralButton("Cancel") { dialog, _ ->
                                dialog.cancel()
                            }
                            val mDialog = builder.create()
                            mDialog.show()
                        }
                    } else {
                        startButton.visibility = View.VISIBLE
                        startButton.isEnabled = true
                        startButton.setOnClickListener { startGame(lobbyName) }
                    }
                } else {
                    leaveButton.visibility = View.VISIBLE
                    leaveButton.isEnabled = true
                    leaveButton.setOnClickListener { leaveGame(lobbyName) }
                }
            }
            userListAdapter.addUsers(usernames)
        }
    }

    private fun addBot(botName: String){
        val lobby = JSONObject()
        lobby.put("username", botName)
        lobby.put("lobbyName", lobbyName)
        gameController.addBot(this, lobby)
    }

    fun removeBot(botName: String) {
        val lobby = JSONObject()
        lobby.put("username", botName)
        lobby.put("lobbyName", lobbyName)
        gameController.removeBot(this, lobby)
    }
}
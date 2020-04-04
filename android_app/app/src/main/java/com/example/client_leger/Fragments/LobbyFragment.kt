package com.example.client_leger.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
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
    lateinit var userListAdapter: UserListViewAdapter
    lateinit var startListener: Disposable;

    private lateinit var lobbyNotifSub: Disposable

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_lobby, container, false)
        username = activity!!.intent.getStringExtra("username")
        val bundle = this.arguments
        lobbyName = ""
        if (bundle != null) {
            lobbyName = bundle.getString("lobbyName")
        }
        gameController.getUsers(this, lobbyName)

        userListAdapter = UserListViewAdapter(this)
        var listview = v.findViewById<ListView>(R.id.userlist)
        listview.adapter = userListAdapter



        startListener = Communication.getGameStartListener().subscribe { res ->
            activity!!.runOnUiThread {
                replaceFragment(GameplayFragment())
            }
        }

        lobbyNotifSub = Communication.getLobbyUpdateListener().subscribe { mes ->
            when (mes.getString("type")) {
                "join" -> {
                    val user = mes.getString("username")

                    if (username == user) {

                    } else {
                        activity!!.runOnUiThread {
                            // Stuff that updates the UI
                            userListAdapter.addUser(user)
                        }

                    }
                }
                "leave" -> {
                    val user = mes.getString("username")
                    activity!!.runOnUiThread {
                        userListAdapter.removeUser(user)
                    }

                }
            }
        }

        return v
    }

    override fun onDestroy() {
        super.onDestroy()
        startListener.dispose()
    }

    private fun startGame(lobbyName: String) {
        gameController.startGame(this, lobbyName)
    }

    private fun leaveGame(lobbyName: String) {
        var body = JSONObject()
        body.put("lobbyName", lobbyName)
        body.put("username", username)
        gameController.leaveGame(this, body)
    }

    override fun replaceFragment(fragment: Fragment) {
        fragmentManager!!.beginTransaction().replace(R.id.container_view_right, fragment)
            .addToBackStack(fragment.toString()).commit()
    }

    fun loadUsers(userJsonArray: JSONArray) {
        for (i in 0 until userJsonArray.length()) {
            usernames.add(userJsonArray.get(i).toString())
        }
        if (usernames.isNotEmpty()) {
            var startButton = view!!.findViewById<Button>(R.id.button_start)
            var leaveButton = view!!.findViewById<Button>(R.id.button_leave)
            var addBotButton = view!!.findViewById<Button>(R.id.button_addBot)
            if (usernames[0] == username) {
                startButton.visibility = View.VISIBLE
                startButton.isEnabled = true
                startButton.setOnClickListener { startGame(lobbyName) }

                addBotButton.visibility = View.VISIBLE
                addBotButton.isEnabled = true
                addBotButton.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    val bots = arrayOf("bot:olivier", "bot:sebastien", "bot:olivia")
                    builder.setTitle("Select your bot")
                    builder.setSingleChoiceItems(bots,-1){dialogInterface,i->
                        addBot(bots[i])
                        dialogInterface.dismiss()
                    }
                    builder.setNeutralButton("Cancel"){dialog,_->
                        dialog.cancel()
                    }
                    val mDialog = builder.create()
                    mDialog.show()
                }
            } else {
                leaveButton.visibility = View.VISIBLE
                leaveButton.isEnabled = true
                leaveButton.setOnClickListener { leaveGame(lobbyName) }
            }
        }
        userListAdapter.addUsers(usernames)
    }

    private fun addBot(botName: String){
        var lobby = JSONObject()
        lobby.put("username", botName)
        lobby.put("lobbyName", lobbyName)
        gameController.addBot(this, lobby)

    }

    fun removeBot(botName: String) {
        var lobby = JSONObject()
        lobby.put("username", botName)
        lobby.put("lobbyName", lobbyName)
        gameController.removeBot(this, lobby)
    }

}
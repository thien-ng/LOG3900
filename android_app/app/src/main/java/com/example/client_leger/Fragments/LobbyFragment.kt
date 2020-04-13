package com.example.client_leger.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.example.client_leger.Adapters.UserListViewAdapter
import com.example.client_leger.Communication.Communication
import com.example.client_leger.Controller.GameController
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.R
import com.example.client_leger.models.Game
import com.example.client_leger.models.GameMode
import com.example.client_leger.models.Lobby
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_lobby.*
import kotlinx.android.synthetic.main.fragment_lobby.view.*
import kotlinx.android.synthetic.main.fragment_lobby.view.textView_FullLobby
import kotlinx.android.synthetic.main.fragment_profil.view.*
import org.json.JSONArray
import org.json.JSONObject


class LobbyFragment() : Fragment(),
    FragmentChangeListener {
    private var gameController: GameController = GameController()
    private var isMaster: Boolean = false
    lateinit var username: String
    private var  bots = arrayListOf("bot:olivier", "bot:sebastien", "bot:olivia")
    private lateinit var v: View
    private lateinit var userListAdapter: UserListViewAdapter
    private lateinit var startListener: Disposable
    private lateinit var lobbyNotifSub: Disposable
    private lateinit var kickNotifSub: Disposable
    private lateinit var lobby: Lobby

    private lateinit var startButton: Button
    private lateinit var leaveButton: Button
    private lateinit var addBotButton: Button
    private lateinit var inviteButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_lobby, container, false)
        activity!!.findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility = View.GONE
        username = activity!!.intent.getStringExtra("username")
        val bundle = this.arguments
        if (bundle != null) {
            isMaster = bundle.getBoolean("isMaster")
            lobby = bundle.getSerializable("lobby") as Lobby
        }
        v.textView_gameModeName.text = lobby.gameMode

        startButton = v.findViewById<Button>(R.id.button_start)
        leaveButton = v.findViewById<Button>(R.id.button_leave)
        addBotButton = v.findViewById<Button>(R.id.button_addBot)
        inviteButton = v.findViewById<Button>(R.id.button_invitePlayer)

        gameController.getUsers(this, lobby.lobbyName, lobby.gameMode)
        userListAdapter = UserListViewAdapter(this, isMaster)
        val listview = v.findViewById<ListView>(R.id.userlist)
        listview.adapter = userListAdapter

        startListener = Communication.getGameStartListener().subscribe {
            activity!!.runOnUiThread {
                replaceFragment(GameplayFragment())
            }
        }

        kickNotifSub = Communication.getUpdateKickListener().subscribe{
            activity!!.runOnUiThread {
                Toast.makeText(context, "You have been kicked", Toast.LENGTH_SHORT).show()
            }
        }

        lobbyNotifSub = Communication.getLobbyUpdateListener().subscribe { mes ->
            when (mes.getString("type")) {
                "join" -> {
                    if(mes.getString("lobbyName") == lobby.lobbyName) {
                        val user = mes.getString("username")

                        if (username != user) {
                            activity!!.runOnUiThread {
                                if (user.startsWith("bot:")) {
                                    userListAdapter.addBot(user)
                                    bots.remove(user)
                                } else userListAdapter.addUser(user)
                                if(isMaster && lobby.gameMode == GameMode.FFA.toString()) {
                                    if (userListAdapter.count >= 2) {
                                        v.textView_NotEnoughPlayers.visibility = View.GONE
                                        startButton.visibility = View.VISIBLE
                                        startButton.isEnabled = true
                                        startButton.setOnClickListener { startGame(lobby.lobbyName) }
                                    } else {
                                        v.textView_NotEnoughPlayers.visibility = View.VISIBLE
                                        startButton.visibility = View.GONE
                                        startButton.isEnabled = false
                                    }
                                    if(userListAdapter.count == lobby.size){
                                        inviteButton.visibility = View.INVISIBLE
                                        inviteButton.isEnabled = false
                                        if(lobby.gameMode == GameMode.FFA.toString()){
                                            addBotButton.visibility = View.INVISIBLE
                                            addBotButton.isEnabled = false
                                        }
                                        textView_FullLobby.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }
                    }
                }
                "leave" -> {
                    if(mes.getString("lobbyName") == lobby.lobbyName) {
                        val user = mes.getString("username")
                        if (user != username) {
                            activity!!.runOnUiThread {
                                if (user.startsWith("bot:")) {
                                    bots.add(user)
                                }
                                userListAdapter.removePlayer(user, username)
                                setMasterView(lobby.gameMode)
                            }
                        } else {
                            fragmentManager!!.beginTransaction()
                                .replace(R.id.container_view_right, LobbyCardsFragment()).commit()
                        }
                    }
                }
            }
        }
        return v
    }

    fun checkIsMaster():Boolean {
        var i =0;
        while(userListAdapter.getItem(i).startsWith("bot:")){
            i++
        }
        isMaster =  userListAdapter.getItem(i) == username
        return isMaster
    }

    override fun onDestroy() {
        activity!!.findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility = View.VISIBLE
        super.onDestroy()
        startListener.dispose()
        lobbyNotifSub.dispose()
        kickNotifSub.dispose()
    }

    private fun startGame(lobbyName: String) {
        gameController.startGame(this, lobbyName)
    }

    private fun leaveGame(lobbyName: String) {
        val body = JSONObject()
        body.put("lobbyName", lobbyName)
        body.put("username", username)
        gameController.leaveGame(this, body, fragmentManager!!)
    }

    override fun replaceFragment(fragment: Fragment) {
        fragmentManager!!.beginTransaction().replace(R.id.container_view_right, fragment).commit()
    }

    fun setView(userJsonArray: JSONArray, mode: String) {
        activity!!.runOnUiThread {
            var usernames: ArrayList<String> = arrayListOf()
            for (i in 0 until userJsonArray.length()) {
                if (userJsonArray.get(i).toString().startsWith("bot:")) {
                    userListAdapter.addBot(userJsonArray.get(i).toString())
                    bots.remove(userJsonArray.get(i).toString())
                } else usernames.add(userJsonArray.get(i).toString())
            }
            if (usernames.isNotEmpty()) {
                leaveButton.setOnClickListener { leaveGame(lobby.lobbyName) }
                if (isMaster) {
                    setMasterView(mode)
                }else{
                    v.textView_WaitingForLeader.visibility = View.VISIBLE
                }
            }
            userListAdapter.addUsers(usernames)
        }
    }
    private fun setMasterView(mode:String){
        v.textView_WaitingForLeader.visibility = View.GONE
        v.textView_FullLobby.visibility = View.GONE
        v.textView_NotEnoughPlayers.visibility = View.GONE

        if (mode == "FFA") {
            if(userListAdapter.count >= 2){
                startButton.visibility = View.VISIBLE
                startButton.isEnabled = true
                startButton.setOnClickListener { startGame(lobby.lobbyName) }
            }else{
                startButton.visibility = View.INVISIBLE
                startButton.isEnabled = false
                v.textView_NotEnoughPlayers.visibility = View.VISIBLE
            }
            addBotButton.visibility = View.VISIBLE
            addBotButton.isEnabled = true
            addBotButton.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Select your bot")
                val array = arrayOfNulls<String>(bots.size)
                builder.setSingleChoiceItems(bots.toArray(array), -1) { dialogInterface, i ->
                    addBot(bots[i])
                    dialogInterface.dismiss()
                }
                builder.setNeutralButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                val mDialog = builder.create()
                mDialog.show()
            }
        }else{
            startButton.visibility = View.VISIBLE
            startButton.isEnabled = true
            startButton.setOnClickListener { startGame(lobby.lobbyName) }
        }
        if(mode != "SOLO") {
            inviteButton.visibility = View.VISIBLE
            inviteButton.isEnabled = true
            inviteButton.setOnClickListener{
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Invite player")

                gameController.getOnlineUsers(this, builder, username)

                builder.setNeutralButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
            }
        }
    }
    private fun addBot(botName: String){
        val body = JSONObject()
        body.put("username", botName)
        body.put("lobbyName", lobby.lobbyName)
        gameController.addBot(this, body)
    }

    fun removePlayer(botName: String) {
        val body = JSONObject()
        body.put("username", botName)
        body.put("lobbyName", lobby.lobbyName)
        body.put("isKicked", true)
        gameController.removePlayer(this, body)
    }

    fun invitePlayer(username: String) {
        val invitation = JSONObject()
        invitation.put("username", username)
        invitation.put("lobbyName", lobby.lobbyName)
        gameController.invitePlayer(this, invitation)
    }
}
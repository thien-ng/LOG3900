package com.example.client_leger

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.example.client_leger.Fragments.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var chatFragment = ChatFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationListener)
        supportFragmentManager.beginTransaction().replace(R.id.container_view_left, chatFragment).commit()
        bottomNavigationView.selectedItemId = R.id.action_game
    }

    override fun onBackPressed() {
        SocketIO.disconnect()

        finish()
    }

    private val onNavigationListener = BottomNavigationView.OnNavigationItemSelectedListener  {menuItem ->
        when (menuItem.itemId) {
            R.id.action_profil -> {
                supportFragmentManager.beginTransaction().replace(R.id.container_view_right, ProfilFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.action_game -> {
                supportFragmentManager.beginTransaction().replace(R.id.container_view_right, LobbyCardsFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        return@OnNavigationItemSelectedListener false
    }

}



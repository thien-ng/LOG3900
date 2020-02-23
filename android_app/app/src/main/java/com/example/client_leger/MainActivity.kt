package com.example.client_leger

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import com.example.client_leger.Fragments.ChatFragment
import com.example.client_leger.Fragments.DrawFragment
import com.example.client_leger.Fragments.ProfilFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationListener)
        bottomNavigationView.selectedItemId = R.id.action_chat
    }

    override fun onBackPressed() {
        // To ignore back button pressed.
    }

    private val onNavigationListener = BottomNavigationView.OnNavigationItemSelectedListener  {menuItem ->
        when (menuItem.itemId) {
            R.id.action_chat -> {
                supportFragmentManager.beginTransaction().replace(R.id.container_view, ChatFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.action_profil -> {
                supportFragmentManager.beginTransaction().replace(R.id.container_view, ProfilFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.action_game -> {
                supportFragmentManager.beginTransaction().replace(R.id.container_view, DrawFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        return@OnNavigationItemSelectedListener false
    }

}



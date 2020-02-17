package com.example.client_leger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.client_leger.Fragments.ChatFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(R.id.container_view, ChatFragment()).commit()
    }

    override fun onBackPressed() {
        // To ignore back button pressed.
    }

}



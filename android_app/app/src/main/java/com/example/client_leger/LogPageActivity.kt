package com.example.client_leger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import kotlinx.android.synthetic.main.activity_logpage.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_registration.*


class LogPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_registration)
        setSupportActionBar(toolbar)


        register_button.setOnClickListener {
            //TODO: Create an account for the user.
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        textView_alreadyHaveAccount.setOnClickListener {
            setContentView(R.layout.fragment_login)

            login_button.setOnClickListener {
                //TODO: Login the user.
                val intent = Intent(this, ChatActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}

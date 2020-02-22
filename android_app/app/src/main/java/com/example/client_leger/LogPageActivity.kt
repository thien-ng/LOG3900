package com.example.client_leger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.client_leger.Fragments.LoginFragment
import kotlinx.android.synthetic.main.activity_logpage.*


class LogPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logpage)
        setSupportActionBar(toolbar)

        supportFragmentManager.beginTransaction().replace(R.id.container_view, LoginFragment()).commit()
    }
}

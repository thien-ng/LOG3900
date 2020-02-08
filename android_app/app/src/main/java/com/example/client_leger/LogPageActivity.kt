package com.example.client_leger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import android.content.Intent
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_logpage.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_registration.*
import org.json.JSONObject

class LogPageActivity : AppCompatActivity() {
    private var controller = ConnexionController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_registration)
        setSupportActionBar(toolbar)
        register_button.setOnClickListener {
            if(register_editText_name.text.isNotBlank() && register_editText_password.text.isNotBlank()) {
                var body = JSONObject()
                body.accumulate("username", register_editText_name.text.toString().trim())
                body.accumulate("password", register_editText_password.text.toString().trim())
                controller.registerUser(this, this, body)
            }else {
                Toast.makeText(
                    applicationContext,
                    "Veuillez remplir tout les champs",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        textView_alreadyHaveAccount.setOnClickListener {
            setContentView(R.layout.fragment_login)

            login_button.setOnClickListener {
                if(register_editText_name.text.isNotBlank() && register_editText_password.text.isNotBlank()) {
                    var body = JSONObject()
                    body.accumulate("username", login_editText_name.text.toString().trim())
                    body.accumulate("password", login_editText_password.text.toString().trim())
                    controller.loginUser(this, this, body)
                }else {
                    Toast.makeText(
                        applicationContext,
                        "Veuillez remplir tout les champs",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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

    fun connect(username: String){
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }
    
}

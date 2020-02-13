package com.example.client_leger

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import android.content.Intent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_logpage.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_registration.*
import org.json.JSONObject

class LogPageActivity : AppCompatActivity() {
    private var controller = ConnexionController()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)
        setSupportActionBar(toolbar)
        login_button.isEnabled = true
        login_button.setOnClickListener {
            if (login_editText_name.text.isNotBlank() && login_editText_password.text.isNotBlank()) {
                login_button.isEnabled = false
                var body = JSONObject()
                body.accumulate("username", login_editText_name.text.toString().trim())
                body.accumulate("password", login_editText_password.text.toString().trim())
                controller.loginUser(this, this, body)

            } else {
                Toast.makeText(
                    applicationContext,
                    "Veuillez remplir tout les champs",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        textView_dontHaveAccount.setOnClickListener {
            setContentView(R.layout.fragment_registration)
            register_button.isEnabled = true
            register_button.setOnClickListener {

                if(validRegisterFields()) {
                    closeKeyboard()
                    register_button.isEnabled = false
                    var body = JSONObject()
                    body.accumulate("username", register_editText_name.text.toString().trim())
                    body.accumulate(
                        "password",
                        register_editText_password.text.toString().trim()
                    )
                    controller.registerUser(this, this, body)
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

    fun connect(username: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }
    private fun closeKeyboard(){
        if ( this.currentFocus != null){
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus.windowToken, 0)
        }
    }

    private fun validRegisterFields(): Boolean{
        when {
            register_editText_name.text.isBlank() -> {
                register_editText_name.error = "Enter a valid name"
                register_editText_name.requestFocus()
                return false
            }
            register_editText_password.text.isBlank() -> {
                register_editText_password.error = "Enter a valid password"
                register_editText_password.requestFocus()
                return false
            }
            register_editText_confirmPassword.text.isBlank() -> {
                register_editText_confirmPassword.error = "You need to confirm the password"
                register_editText_confirmPassword.requestFocus()
                return false
            }
            register_editText_confirmPassword.text.toString() != register_editText_password.text.toString() -> {
                register_editText_confirmPassword.error = "Password does not match"
                register_editText_confirmPassword.requestFocus()
                return false
            }
            else -> return true
        }
    }

}

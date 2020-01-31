package com.example.client_leger

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import android.view.Menu
import android.view.MenuItem
import com.android.volley.AuthFailureError
import com.android.volley.toolbox.Volley

import android.content.Intent
import kotlinx.android.synthetic.main.activity_logpage.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_registration.*
import org.json.JSONObject
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest

class LogPageActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_registration)
        setSupportActionBar(toolbar)

        register_button.setOnClickListener {
            var body = JSONObject()
            body.accumulate("username",register_editText_name.text.toString())
            body.accumulate("password", register_editText_password.text.toString())
            val endpoint = "/account/register"
            var status = authenticateUser( body, endpoint )
            if(status == 200) {
              val intent = Intent(this, MainActivity::class.java)
              intent.putExtra("username",register_editText_name.text.toString())
              startActivity(intent)
            }
        }

        textView_alreadyHaveAccount.setOnClickListener {
            setContentView(R.layout.fragment_login)

            login_button.setOnClickListener {
                var body = JSONObject()
                body.accumulate("username",login_editText_name.text.toString())
                body.accumulate("password", login_editText_password.text.toString())
                var endpoint = "/account/login"
                var status = authenticateUser( body,endpoint )
                if(status == 200) {
                  val intent = Intent(this, MainActivity::class.java)
                  intent.putExtra("username", login_editText_name.text.toString())
                  startActivity(intent)
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
    private fun authenticateUser (body: JSONObject, endpoint: String): Int {

        var status = 504
        var mRequestQueue = Volley.newRequestQueue(this)

        var mStringRequest = object : JsonObjectRequest( Method.POST, Constants.SERVER_URL + endpoint,null, Response.Listener { response ->
            Toast.makeText(applicationContext, response["message"].toString(), Toast.LENGTH_SHORT).show()
            status =  response["status"].toString().toInt()
        }, Response.ErrorListener {
            Toast.makeText(applicationContext, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }) {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                return body.toString().toByteArray()
            }

        }
        mRequestQueue!!.add(mStringRequest)
        return status
    }
}

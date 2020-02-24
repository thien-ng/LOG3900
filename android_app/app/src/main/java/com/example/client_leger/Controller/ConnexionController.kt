package com.example.client_leger

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.Fragments.LoginFragment
import com.example.client_leger.Fragments.RegisterFragment
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_registration.*
import org.json.JSONObject

class ConnexionController {

    fun loginUser(activity: LoginFragment, applicationContext: Context, body: JSONObject){
        var mRequestQueue = Volley.newRequestQueue(applicationContext)

        var mStringRequest = object : JsonObjectRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.LOGIN_ENPOINT,
            null,
            Response.Listener { response ->
                if(response["status"].toString().toInt() == 200) {
                    SocketIO.connect(body.get("username").toString())
                }
                else
                    activity.login_button.isEnabled = true
            },
            Response.ErrorListener {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong...",
                    Toast.LENGTH_SHORT
                ).show()
                activity.login_button.isEnabled = true
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

    }
    fun registerUser(activity: RegisterFragment, applicationContext: Context, body: JSONObject){
        var mRequestQueue = Volley.newRequestQueue(applicationContext)

        var mStringRequest = object : JsonObjectRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.REGISTER_ENDPOINT,
            null,
            Response.Listener { response ->
                Toast.makeText(
                    applicationContext,
                    response["message"].toString(),
                    Toast.LENGTH_SHORT
                ).show()
                if(response["status"].toString().toInt() == 200)
                    SocketIO.connect(body.get("username").toString())
                else
                    activity.register_button.isEnabled = true
            },
            Response.ErrorListener {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong...",
                    Toast.LENGTH_SHORT
                ).show()
                activity.register_button.isEnabled = true
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
    }

}
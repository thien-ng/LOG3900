package com.example.client_leger

import android.content.Context
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ConnexionController {

    fun loginUser(activity: LogPageActivity,applicationContext: Context,body: JSONObject){

        var mRequestQueue = Volley.newRequestQueue(applicationContext)

        var mStringRequest = object : JsonObjectRequest(
            Method.POST,
            Constants.SERVER_URL + Constants.LOGIN_ENPOINT,
            null,
            Response.Listener { response ->
                Toast.makeText(
                    applicationContext,
                    response["message"].toString(),
                    Toast.LENGTH_SHORT
                ).show()
                if(response["status"].toString().toInt() == 200)
                    activity.connect()
            },
            Response.ErrorListener {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong...",
                    Toast.LENGTH_SHORT
                ).show()
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
    fun registerUser(activity: LogPageActivity,applicationContext: Context,body: JSONObject){
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
                if(response["status"].toString().toInt() == 200){
                    activity.register()
                }
            },
            Response.ErrorListener {
                Toast.makeText(
                    applicationContext,
                    "Something went wrong...",
                    Toast.LENGTH_SHORT
                ).show()
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
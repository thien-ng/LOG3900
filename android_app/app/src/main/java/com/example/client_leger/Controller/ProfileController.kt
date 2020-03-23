package com.example.client_leger.Controller

import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.Constants
import com.example.client_leger.Fragments.ProfilFragment
import com.example.client_leger.models.User

class ProfileController {
    fun getUserProfile(fragment: ProfilFragment, username:String){
        val requestQueue = Volley.newRequestQueue(fragment.context)

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.USER_INFO_ENDPOINT + username,
            null,
            Response.Listener { response ->
                fragment.binding.user = User(username, response.getString("firstName"), response.getString("lastName"), response.getJSONArray("connections"))
                (0 until fragment.binding!!.user!!.connections!!.length()).forEach { i ->
                    var item = fragment.binding!!.user!!.connections.getJSONObject(i)
                    if(item.getBoolean("isLogin")) {
                        fragment.adapter.addLoggedIn(item.getString("times"))
                    } else {
                        fragment.adapter.addLoggedOut(item.getString("times"))
                    }

                    fragment.adapter.getItemViewType(i)
                }
            }, Response.ErrorListener{
                    error ->
                Toast.makeText(
                    fragment.context,
                    error.message,
                    Toast.LENGTH_SHORT
                ).show()

            }
        )

        requestQueue.add(jsonObjectRequest)
    }

}
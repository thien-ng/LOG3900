package com.example.client_leger.Controller

import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.example.client_leger.Constants
import com.example.client_leger.Fragments.GameCardsFragment
import org.json.JSONArray

class GameCardsController {
    fun getGameCards(activity: GameCardsFragment) {
        val requestQueue = Volley.newRequestQueue(activity.context)

        val jsonObjectRequest = JsonArrayRequest(
            Request.Method.GET,
            Constants.SERVER_URL + Constants.CARD_ENDPOINT,
            null,
            Response.Listener<JSONArray> { response ->
                if (response.length() > 0) activity.loadGameCards(activity.adapter, response)
            }, Response.ErrorListener { error ->
                Toast.makeText(activity.context, error.message, Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(jsonObjectRequest)
    }
}
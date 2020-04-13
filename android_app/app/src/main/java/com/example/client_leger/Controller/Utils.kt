package com.example.client_leger.Controller

import android.util.Log
import com.android.volley.VolleyError
import org.json.JSONObject

class Utils {
    companion object {
        fun getErrorMessage(error: VolleyError): String {
            val jsonError = error.networkResponse.data.toString()
            Log.d("error",jsonError)
            val responseObject = JSONObject(jsonError)
            return responseObject.optString("message")
        }
    }
}
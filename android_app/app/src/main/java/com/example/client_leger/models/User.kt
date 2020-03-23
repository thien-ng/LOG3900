package com.example.client_leger.models

import org.json.JSONArray

class User(username:String, fName:String, lName:String, connections: JSONArray) {
    var name = username
    var fName = fName
    var lName = lName

    var connections = connections
}
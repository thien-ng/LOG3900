package com.example.client_leger.Fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.client_leger.ConnexionController
import com.example.client_leger.MainActivity
import com.example.client_leger.R
import kotlinx.android.synthetic.main.fragment_registration.*
import kotlinx.android.synthetic.main.fragment_registration.view.*
import org.json.JSONObject

class RegisterFragment: Fragment() {

    private var controller = ConnexionController()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_registration, container, false)

        v.register_button.setOnClickListener {
            v.register_button.isEnabled = true
            v.register_button.setOnClickListener {

                if(validRegisterFields()) {
                    closeKeyboard()
                    v.register_button.isEnabled = false

                    var body = JSONObject( mapOf(
                        "username" to v.register_editText_name.text.toString().trim(),
                        "password" to v.register_editText_password.text.toString().trim()
                    ))

                    controller.registerUser(this, activity!!.applicationContext, body)
                }
            }
        }

        return v
    }

    fun connect(username: String) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }


    private fun closeKeyboard(){
//        if ( this.currentFocus != null){
//            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(this.currentFocus.windowToken, 0)
//        }
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
                register_editText_confirmPassword.error = "Password doesn't match"
                register_editText_confirmPassword.requestFocus()
                return false
            }
            else -> return true
        }
    }

}
package com.example.client_leger.Fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.client_leger.ConnexionController
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.MainActivity
import com.example.client_leger.R
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_registration.*
import org.json.JSONObject

class LoginFragment : Fragment(), FragmentChangeListener {

    private var controller = ConnexionController()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_login, container, false)

        v.login_button.isEnabled = true
        v.login_button.setOnClickListener {
            if (v.login_editText_name.text.isNotBlank() && v.login_editText_password.text.isNotBlank()) {
                v.login_button.isEnabled = false

                var body = JSONObject( mapOf(
                    "username" to v.login_editText_name.text.toString().trim(),
                    "password" to v.login_editText_password.text.toString().trim()
                ))

                controller.loginUser(this, activity!!.applicationContext, body)

            } else {
                Toast.makeText(
                    activity,
                    "Veuillez remplir tout les champs",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        v.textView_dontHaveAccount.setOnClickListener {
            replaceFragment(RegisterFragment())
        }

        return v
    }

    override fun replaceFragment(fragment: Fragment) {
         fragmentManager!!.beginTransaction().replace(R.id.container_view, fragment).addToBackStack(fragment.toString()).commit()
    }


    fun connect(username: String) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

}
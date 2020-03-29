package com.example.client_leger.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.client_leger.Communication.Communication
import com.example.client_leger.ConnexionController
import com.example.client_leger.Interface.FragmentChangeListener
import com.example.client_leger.LogState
import com.example.client_leger.MainActivity
import com.example.client_leger.R
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_login.view.*
import org.json.JSONObject


class LoginFragment : Fragment(), FragmentChangeListener {

    private var controller = ConnexionController()
    private lateinit var connexionListener: Disposable
    lateinit var username: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_login, container, false)

        v.login_button.isEnabled = true
        v.login_button.setOnClickListener {
            if (validateLoginFields(v)) {
                closeKeyboard()
                v.login_button.isEnabled = false

                val body = JSONObject( mapOf(
                    "username" to v.login_editText_name.text.toString().trim(),
                    "password" to v.login_editText_password.text.toString().trim()
                ))

                username = v.login_editText_name.text.toString().trim()

                controller.loginUser(this, activity!!.applicationContext, body)

                LogState.isLoginState = true

            }
        }

        v.textView_dontHaveAccount.setOnClickListener {
            replaceFragment(RegisterFragment())
        }

        connexionListener = Communication.getConnectionListener().subscribe{mes ->
            handleConnection(mes)
        }

        return v
    }

    private fun handleConnection(mes: JSONObject) {
        if (!LogState.isLoginState) return
        activity!!.runOnUiThread{
            Toast.makeText(this.context, mes["message"].toString(), Toast.LENGTH_SHORT).show()
            if (::username.isInitialized) {
                if (mes.getString("status").toInt() == 200) {
                    connect(username)
                }
            }
        }
    }

    private fun closeKeyboard(){
        if ( activity!!.currentFocus != null){
            val imm: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity!!.currentFocus.windowToken, 0)
        }
    }

    override fun replaceFragment(fragment: Fragment) {
         fragmentManager!!.beginTransaction().replace(R.id.container_view, fragment).addToBackStack(fragment.toString()).commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        connexionListener.dispose()
    }

    private fun connect(username: String) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    private fun validateLoginFields(v: View):Boolean{
        return when{
            v.login_editText_name.text.isBlank() -> {
                v.login_editText_name.error = "Enter a username."
                v.login_editText_name.requestFocus()
                false
            }

            !isStringAlphanumeric(v.login_editText_name.text.toString()) -> {
                v.login_editText_name.error = "Only letters and numbers are accepted."
                v.login_editText_name.requestFocus()
                false
            }

            v.login_editText_password.text.isBlank() -> {
                v.login_editText_password.error = "Enter a password."
                v.login_editText_password.requestFocus()
                false
            }

            !isStringAlphanumeric(v.login_editText_password.text.toString()) -> {
                v.login_editText_password.error = "Only letters and numbers are accepted."
                v.login_editText_password.requestFocus()
                false
            }

            else -> true
        }
    }
}

fun isStringAlphanumeric(string: String) : Boolean {
    for (character in string) {
        if (!Character.isLetterOrDigit(character)) {
            return false
        }
    }

    return true
}
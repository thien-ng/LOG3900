package com.example.client_leger.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.example.client_leger.*
import com.example.client_leger.Communication.Communication
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_registration.*
import kotlinx.android.synthetic.main.fragment_registration.view.*
import org.json.JSONObject
import java.io.FileNotFoundException

class RegisterFragment : Fragment() {

    private var controller = ConnexionController()
    private lateinit var connexionListener: Disposable
    lateinit var username: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_registration, container, false)

        v.register_button.isEnabled = true

        v.register_pickAvatar.setOnClickListener {
            pickFromGallery()
        }

        v.register_button.setOnClickListener {

            if (validRegisterFields(v)) {
                closeKeyboard()
                v.register_button.isEnabled = false

                val body = JSONObject(
                    mapOf(
                        "username" to v.register_editText_username.text.toString().trim(),
                        "password" to v.register_editText_password.text.toString().trim(),
                        "firstName" to v.register_editText_fName.text.toString().trim(),
                        "lastName" to v.register_editText_lName.text.toString().trim()
                    )
                )

                username = v.register_editText_username.text.toString().trim()

                controller.registerUser(this, activity!!.applicationContext, body)

                LogState.isLoginState = false
            }
        }

        connexionListener = Communication.getConnectionListener().subscribe{ mes ->
            handleConnection(mes)
        }

        return v
    }

    private fun handleConnection(mes: JSONObject) {
        if (LogState.isLoginState) return

        activity!!.runOnUiThread{
            if (::username.isInitialized) {
                Toast.makeText(activity, mes.getString("message").toString(), Toast.LENGTH_SHORT).show()
                if (mes.getString("status").toInt() == 200) {
                    connect(username)
                }
            }
        }
    }

    private fun connect(username: String) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        connexionListener.dispose()
    }

    private fun closeKeyboard() {
        if (activity!!.currentFocus != null) {
            val imm: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity!!.currentFocus.windowToken, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode === Activity.RESULT_OK) when (requestCode) {
            Constants.GALLERY_REQUEST_CODE -> {
                val selectedImage: Uri = data?.data!!
                register_pickAvatar.setImageBitmap(this.context?.let { decodeUri(it, selectedImage, 50) })
            }
        }
    }

    @Throws(FileNotFoundException::class)
    fun decodeUri(c: Context, uri: Uri?, requiredSize: Int): Bitmap? {
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(c.contentResolver.openInputStream(uri), null, o)
        var widthTmp = o.outWidth
        var heightTmp = o.outHeight
        var scale = 1
        while (true) {
            if (widthTmp / 2 < requiredSize || heightTmp / 2 < requiredSize) break
            widthTmp /= 2
            heightTmp /= 2
            scale *= 2
        }
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        return BitmapFactory.decodeStream(c.contentResolver.openInputStream(uri), null, o2)
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, Constants.GALLERY_REQUEST_CODE)
    }

    private fun validRegisterFields(v: View): Boolean {
        when {
            v.register_editText_fName.text.isBlank() -> {
                v.register_editText_fName.error = "Enter a valid first name"
                v.register_editText_fName.requestFocus()
                return false
            }
            v.register_editText_lName.text.isBlank() -> {
                v.register_editText_lName.error = "Enter a valid last name"
                v.register_editText_lName.requestFocus()
                return false
            }
            v.register_editText_username.text.isBlank() || v.register_editText_username.text.length > Constants.MAX_USERNAME_SiZE  -> {
                v.register_editText_username.error = "Enter a valid username"
                v.register_editText_username.requestFocus()
                return false
            }
            v.register_editText_password.text.isBlank() || v.register_editText_password.text.length > Constants.MAX_PASSWORD_SiZE -> {
                v.register_editText_password.error = "Enter a valid password"
                v.register_editText_password.requestFocus()
                return false
            }
            v.register_editText_confirmPassword.text.isBlank() -> {
                v.register_editText_confirmPassword.error = "You need to confirm the password"
                v.register_editText_confirmPassword.requestFocus()
                return false
            }
            v.register_editText_confirmPassword.text.toString() != v.register_editText_password.text.toString() -> {
                v.register_editText_confirmPassword.error = "Password doesn't match"
                v.register_editText_confirmPassword.requestFocus()
                return false
            }
            else -> return true
        }
    }
}
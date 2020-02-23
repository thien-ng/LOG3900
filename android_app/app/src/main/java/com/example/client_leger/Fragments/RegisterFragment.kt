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
import com.example.client_leger.ConnexionController
import com.example.client_leger.Constants
import com.example.client_leger.MainActivity
import com.example.client_leger.R
import kotlinx.android.synthetic.main.fragment_registration.*
import kotlinx.android.synthetic.main.fragment_registration.view.*
import org.json.JSONObject
import java.io.FileNotFoundException

class RegisterFragment : Fragment() {

    private var controller = ConnexionController()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_registration, container, false)

        v.register_button.isEnabled = true

        v.register_pickAvatar.setOnClickListener {
            pickFromGallery()
        }

        v.register_button.setOnClickListener {

            if (validRegisterFields()) {
                closeKeyboard()
                v.register_button.isEnabled = false

                var body = JSONObject(
                    mapOf(
                        "username" to v.register_editText_name.text.toString().trim(),
                        "password" to v.register_editText_password.text.toString().trim()
                    )
                )

                controller.registerUser(this, activity!!.applicationContext, body)
            }
        }

        return v
    }

    fun connect(username: String) {
        val intent = Intent(activity, MainActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
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

    private fun validRegisterFields(): Boolean {
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
package com.example.client_leger

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_logpage.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_registration.*
import org.json.JSONObject
import java.io.FileNotFoundException


class LogPageActivity : AppCompatActivity() {
    private var controller = ConnexionController()
    var GALLERY_REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)
        setSupportActionBar(toolbar)
        login_button.isEnabled = true
        login_button.setOnClickListener {
            if (login_editText_name.text.isNotBlank() && login_editText_password.text.isNotBlank()) {
                login_button.isEnabled = false

                var body = JSONObject( mapOf(
                    "username" to login_editText_name.text.toString().trim(),
                    "password" to login_editText_password.text.toString().trim()
                ))

                controller.loginUser(this, this, body)

            } else {
                Toast.makeText(
                    applicationContext,
                    "Veuillez remplir tout les champs",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        textView_dontHaveAccount.setOnClickListener {
            setContentView(R.layout.fragment_registration)
            register_button.isEnabled = true

            register_pickAvatar.setOnClickListener{
                pickFromGallery()
               }

            register_button.setOnClickListener {

                if(validRegisterFields()) {
                    closeKeyboard()
                    register_button.isEnabled = false

                    var body = JSONObject( mapOf(
                        "username" to register_editText_name.text.toString().trim(),
                        "password" to register_editText_password.text.toString().trim()
                    ))

                    controller.registerUser(this, this, body)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode === Activity.RESULT_OK) when (requestCode) {
            GALLERY_REQUEST_CODE -> {
                val selectedImage: Uri = data?.data!!
                register_pickAvatar.setImageBitmap(decodeUri(this,selectedImage, 150))
            }
        }

    }

    private fun pickFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes)
        startActivityForResult(intent,GALLERY_REQUEST_CODE)
    }

    @Throws(FileNotFoundException::class)
    fun decodeUri(
        c: Context,
        uri: Uri?,
        requiredSize: Int
    ): Bitmap? {
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

    fun connect(username: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
    }
    private fun closeKeyboard(){
        if ( this.currentFocus != null){
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus.windowToken, 0)
        }
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

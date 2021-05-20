package com.rpathechicken.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.rpathechicken.MainActivity
import com.rpathechicken.R
import com.rpathechicken.databinding.ActivityLoginBinding
import com.rpathechicken.helpers.SessionManager


class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var session: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session = SessionManager(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initButton()
    }

    private fun initButton() {
        binding.btnLogin.setOnClickListener {
            if(binding.inputUsername.text.isNullOrBlank()
                or binding.inputPassword.text.isNullOrBlank()){
                    snackBarIconInfo(it,"Please fill the blank")
            }else{
                processLogin()
            }
        }
    }

    private fun processLogin() {
        snackBarIconInfo(binding.root,"You are logged in")
        session.createLoginSession("123")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    @SuppressLint("InflateParams")
    private fun snackBarIconInfo(view: View, msg: String) {
        val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT)
        //inflate view
        val customView: View = layoutInflater.inflate(R.layout.snackbar_icon_text, null)
        snackbar.view.setBackgroundColor(Color.TRANSPARENT)
        val snackBarView = snackbar.view as SnackbarLayout
        snackBarView.setPadding(0, 0, 0, 0)
        (customView.findViewById(R.id.message) as TextView).text = msg
        (customView.findViewById(R.id.icon) as ImageView).setImageResource(R.drawable.ic_error_outline)
        snackBarView.addView(customView, 0)
        snackbar.show()
    }
}
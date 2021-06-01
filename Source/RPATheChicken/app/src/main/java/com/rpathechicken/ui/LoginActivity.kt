package com.rpathechicken.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.rpathechicken.MainActivity
import com.rpathechicken.R
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.ActivityLoginBinding
import com.rpathechicken.helpers.SessionManager
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var session: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session = SessionManager(this)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initButton()
        initComponen()
    }

    private fun initComponen() {
        binding.layoutProgress.progressOverlay.visibility = View.GONE
    }

    private fun initButton() {
        binding.btnLogin.setOnClickListener {
            if(binding.inputUsername.text.isNullOrBlank()){
                    snackBarIconInfo(it,"Please fill the blank")
                binding.inputUsername.error = "Please fill the blank"
            }else if(binding.inputPassword.text.isNullOrBlank()){
                    snackBarIconInfo(it,"Please fill the blank")
                binding.inputPassword.error = "Please fill the blank"
            }else{
                binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
                processLogin()
            }
        }
    }

    private fun processLogin() {
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.post(ApiEndPoint.login)
            .addBodyParameter("email",binding.inputUsername.text.toString())
            .addBodyParameter("password",binding.inputPassword.text.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    val token = response!!.getString("token").toString()

                    val user = response.getJSONObject("user")

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    session.createLoginSession(token)
                    session.username = user.getString("username")
                    session.fullname = user.getString("name")
                    session.phone = user.getString("phone")
                    session.email = user.getString("email")
                    session.userId = user.getInt("id").toString()
                    session.managerId = user.getInt("sup_id").toString()
                    startActivity(intent)
                    finish()

                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("login-msg", anError!!.message.toString())
                    Log.d("login-detail", anError.errorDetail)
                    Log.d("login-body",anError.errorBody)
                    Log.d("login-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    var errMsg: String = if(error.isNullOrEmpty() || error.isBlank()){
                        anError.errorDetail.toString()
                    }else{
                        error
                    }

                    /*SweetAlertDialog(this@LoginActivity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText(error)
                        .show()*/

                    val alertDialog =
                        SweetAlertDialog(this@LoginActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = errMsg
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@LoginActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
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
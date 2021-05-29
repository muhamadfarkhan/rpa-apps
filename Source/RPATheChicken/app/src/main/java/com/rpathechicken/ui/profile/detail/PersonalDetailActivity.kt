package com.rpathechicken.ui.profile.detail

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.rpathechicken.R
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.ActivityPersonalDetailBinding
import com.rpathechicken.helpers.SessionManager
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class PersonalDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPersonalDetailBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session = SessionManager(this)

        binding = ActivityPersonalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        getProfileData()
    }

    private fun getProfileData() {

        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.profile)
            .addHeaders("Authorization", session.token)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("login-data", response!!.toString())

                    val user = response.getJSONObject("user")

                    binding.etEmail.setText(user.getString("email"))
                    binding.etUsername.setText(user.getString("username"))
                    binding.etPhone.setText(user.getString("phone"))


                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("login-msg", anError!!.message.toString())
                    Log.d("login-detail", anError.errorDetail)
                    Log.d("login-body",anError.errorBody)
                    Log.d("login-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    val alertDialog =
                        SweetAlertDialog(this@PersonalDetailActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@PersonalDetailActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

    private fun initToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Personal Details"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                this.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
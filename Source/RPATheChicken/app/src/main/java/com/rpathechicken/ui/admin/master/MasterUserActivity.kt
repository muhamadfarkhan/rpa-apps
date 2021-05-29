package com.rpathechicken.ui.admin.master

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.rpathechicken.R
import com.rpathechicken.adapter.AdapterListAnimation
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.ActivityMasterUserBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.model.User
import com.rpathechicken.utils.ItemAnimation
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class MasterUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMasterUserBinding
    private lateinit var session: SessionManager
    private lateinit var recyclerViewUser: RecyclerView
    private lateinit var mAdapter: AdapterListAnimation
    val items = ArrayList<User>()
    private val animationType: Int = ItemAnimation.BOTTOM_UP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        session = SessionManager(this)

        binding = ActivityMasterUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initComponent()
        getListUser()
    }

    private fun initComponent() {

        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE

        recyclerViewUser = binding.recyclerView
        recyclerViewUser.layoutManager = LinearLayoutManager(this)
        recyclerViewUser.setHasFixedSize(true)

        binding.fabAddUser.setOnClickListener {
            session.isCreate = true
            startActivity(Intent(this, StoreUserActivity::class.java))
        }

    }

    private fun initToolbar() {

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Master User"
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

    private fun getListUser() {

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.user_list)
            .addHeaders("Authorization", session.token)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("login-data", response!!.toString())

                    val user = response.getJSONArray("users")

                    Log.d("login-data", user.toString())

                    items.clear()

                    for (i in 0 until user.length()) {

                        items.add(
                            User(user.getJSONObject(i).getInt("id"),
                                user.getJSONObject(i).getString("username"),
                                user.getJSONObject(i).getString("email"))
                        )

                    }

                    mAdapter = AdapterListAnimation(applicationContext, items, animationType)
                    recyclerViewUser.adapter = mAdapter

                    mAdapter.setOnItemClickListener { _, obj, _ ->
                        //Toast.makeText(applicationContext,obj.username,Toast.LENGTH_LONG).show()
                        session.idEditData = obj.id
                        session.isCreate = false
                        startActivity(Intent(applicationContext, StoreUserActivity::class.java))
                    }

                    mAdapter.setmOnItemDestroyListener { _, obj, _ ->
                        //Toast.makeText(applicationContext,"delete " + obj.username, Toast.LENGTH_LONG).show()
                        confirmDelete(obj.id)
                    }
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
                        SweetAlertDialog(this@MasterUserActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@MasterUserActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

    private fun confirmDelete(id: Int) {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Are you sure?")
            .setContentText("Won't be able to recover this file!")
            .setConfirmText("Yes,delete it!")
            .setConfirmClickListener { sDialog -> sDialog.dismissWithAnimation()
                deleteUser(id) }
            .setCancelButton(
                "Cancel"
            ) { sDialog -> sDialog.dismissWithAnimation() }
            .show()
    }

    private fun deleteUser(id: Int) {
        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = getString(R.string.delete_data)

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.post(ApiEndPoint.user_destroy)
            .addHeaders("Authorization", session.token)
            .addBodyParameter("id", id.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("data-rpa-detail", response!!.toString())

                    val alertDialog = SweetAlertDialog(this@MasterUserActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Well Done..."
                    alertDialog.contentText = response.getString("message")
                    alertDialog.show()

                    getListUser()

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
                        SweetAlertDialog(this@MasterUserActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@MasterUserActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

    override fun onResume() {
        super.onResume()
        getListUser()
    }
}
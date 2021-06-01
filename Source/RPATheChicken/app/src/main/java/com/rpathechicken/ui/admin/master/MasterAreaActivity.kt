package com.rpathechicken.ui.admin.master

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
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
import com.rpathechicken.databinding.ActivityMasterAreaBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.model.Default
import com.rpathechicken.utils.ItemAnimation
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MasterAreaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMasterAreaBinding
    private lateinit var session: SessionManager
    private lateinit var recyclerViewUser: RecyclerView
    private lateinit var mAdapter: AdapterListAnimation
    val items = ArrayList<Default>()
    private val animationType: Int = ItemAnimation.BOTTOM_UP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        binding = ActivityMasterAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initComponent()
        getListRpa()
    }

    private fun initComponent() {

        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE

        recyclerViewUser = binding.recyclerView
        recyclerViewUser.layoutManager = LinearLayoutManager(this)
        recyclerViewUser.setHasFixedSize(true)

        binding.fabAddArea.setOnClickListener {
            session.isCreate = true
            startActivity(Intent(this, StoreAreaActivity::class.java))
        }

    }

    private fun initToolbar() {

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Master Area"
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

    private fun getListRpa() {

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.area_list)
            .addHeaders("Authorization", session.token)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("login-data", response!!.toString())

                    val user = response.getJSONArray("areas")

                    Log.d("login-data", user.toString())

                    items.clear()

                    for (i in 0 until user.length()) {

                        items.add(
                            Default(user.getJSONObject(i).getInt("id"),
                                user.getJSONObject(i).getString("name"),
                                user.getJSONObject(i).getString("address") +
                                        " (" + user.getJSONObject(i).getString("rpa_name") + ")"
                                ,""
                            )
                        )

                    }

                    mAdapter = AdapterListAnimation(applicationContext, items, animationType, false)
                    recyclerViewUser.adapter = mAdapter

                    mAdapter.setOnItemClickListener { _, obj, _ ->
                        //Toast.makeText(applicationContext,obj.username, Toast.LENGTH_LONG).show()
                        session.idEditData = obj.id
                        session.isCreate = false
                        startActivity(Intent(applicationContext, StoreAreaActivity::class.java))
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
                        SweetAlertDialog(this@MasterAreaActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@MasterAreaActivity,
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
                deleteRPA(id) }
            .setCancelButton(
                "Cancel"
            ) { sDialog -> sDialog.dismissWithAnimation() }
            .show()
    }


    private fun deleteRPA(id: Int) {

        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = getString(R.string.delete_data)

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.post(ApiEndPoint.area_destroy)
            .addHeaders("Authorization", session.token)
            .addBodyParameter("id", id.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("area-", response!!.toString())

                    val alertDialog = SweetAlertDialog(this@MasterAreaActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Well Done..."
                    alertDialog.contentText = response.getString("message")
                    alertDialog.show()

                    getListRpa()

                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("area-error", anError!!.message.toString())
                    Log.d("area-error", anError.errorDetail)
                    Log.d("area-error",anError.errorBody)
                    Log.d("area-error", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    val alertDialog =
                        SweetAlertDialog(this@MasterAreaActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@MasterAreaActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

    override fun onResume() {
        super.onResume()
        getListRpa()
    }
}
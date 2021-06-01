package com.rpathechicken.ui.admin.transaction

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
import com.rpathechicken.databinding.ActivityTonaseHeaderBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.model.Default
import com.rpathechicken.ui.admin.master.StoreRPAActivity
import com.rpathechicken.utils.ItemAnimation
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TonaseHeaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTonaseHeaderBinding
    private lateinit var session: SessionManager
    private lateinit var recyclerViewUser: RecyclerView
    private lateinit var mAdapter: AdapterListAnimation
    val items = ArrayList<Default>()
    private val animationType: Int = ItemAnimation.BOTTOM_UP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        binding = ActivityTonaseHeaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initComponent()
        getListTonaseH()
    }

    private fun initComponent() {


        recyclerViewUser = binding.recyclerView
        recyclerViewUser.layoutManager = LinearLayoutManager(this)
        recyclerViewUser.setHasFixedSize(true)

        binding.fabAddTonaseH.setOnClickListener {
            session.isCreate = true
            startActivity(Intent(this, StoreTonaseHeaderActivity::class.java))
        }

    }

    private fun initToolbar() {

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Data Tonase"
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


    private fun getListTonaseH() {

        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = getString(R.string.getting_data)

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.tonase_header_list)
            .addHeaders("Authorization", session.token)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-data", response!!.toString())

                    val user = response.getJSONArray("tonase_headers")

                    items.clear()

                    Log.d("tonase-data", user.toString())

                    for (i in 0 until user.length()) {

                        items.add(
                            Default(
                                user.getJSONObject(i).getInt("id"),
                                "Tanggal : " + user.getJSONObject(i).getString("processed_at"),
                                "Harga : " + user.getJSONObject(i).getString("price"),
                                "Plat Number : " + user.getJSONObject(i).getString("plat_number")
                            )
                        )

                    }

                    mAdapter = AdapterListAnimation(applicationContext, items, animationType, true)
                    recyclerViewUser.adapter = mAdapter

                    mAdapter.setOnItemClickListener { _, obj, _ ->
                        //Toast.makeText(applicationContext,obj.username, Toast.LENGTH_LONG).show()
                        session.idEditData = obj.id
                        session.isCreate = false
                        startActivity(Intent(applicationContext, TonaseDetailActivity::class.java))
                    }

                    mAdapter.setmOnItemDestroyListener { _, obj, _ ->
                        //Toast.makeText(applicationContext,"delete " + obj.username, Toast.LENGTH_LONG).show()
                        confirmDelete(obj.id)
                    }
                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-msg", anError!!.message.toString())
                    Log.d("tonase-detail", anError.errorDetail)
                    Log.d("tonase-body", anError.errorBody)
                    Log.d("tonase-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    val alertDialog =
                        SweetAlertDialog(this@TonaseHeaderActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@TonaseHeaderActivity,
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
            .setConfirmClickListener { sDialog ->
                sDialog.dismissWithAnimation()
                deleteRPA(id)
            }
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

        AndroidNetworking.post(ApiEndPoint.tonase_header_destroy)
            .addHeaders("Authorization", session.token)
            .addBodyParameter("id", id.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-detail", response!!.toString())

                    val alertDialog =
                        SweetAlertDialog(this@TonaseHeaderActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Well Done..."
                    alertDialog.contentText = response.getString("message")
                    alertDialog.show()

                    getListTonaseH()

                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-msg", anError!!.message.toString())
                    Log.d("tonase-detail", anError.errorDetail)
                    Log.d("tonase-body", anError.errorBody)
                    Log.d("tonase-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    val alertDialog =
                        SweetAlertDialog(this@TonaseHeaderActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@TonaseHeaderActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

    override fun onResume() {
        super.onResume()
        getListTonaseH()
    }
}
package com.rpathechicken.ui.admin.transaction

import android.annotation.SuppressLint
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
import com.rpathechicken.databinding.ActivityTonaseDetailBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.model.Default
import com.rpathechicken.utils.ItemAnimation
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TonaseDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTonaseDetailBinding
    private lateinit var session: SessionManager
    private lateinit var recyclerViewUser: RecyclerView
    private lateinit var mAdapter: AdapterListAnimation
    val items = ArrayList<Default>()
    private val animationType: Int = ItemAnimation.BOTTOM_UP


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        binding = ActivityTonaseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initComponent()
        getListTonaseD(session.idEditData)
    }


    private fun initComponent() {


        recyclerViewUser = binding.recyclerView
        recyclerViewUser.layoutManager = LinearLayoutManager(this)
        recyclerViewUser.setHasFixedSize(true)

        binding.fabAddTonaseD.setOnClickListener {
            session.isCreate = true
            startActivity(Intent(this, StoreTonaseHeaderActivity::class.java))
        }

        getDataTonaseH(session.idEditData)

    }

    private fun initToolbar() {

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Data Tonase Detail"
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

    private fun getListTonaseD(idEditData: Int) {

        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = getString(R.string.getting_data)

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.tonase_header_detail+"/{id}")
            .addHeaders("Authorization", session.token)
            .addPathParameter("id",idEditData.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-data", response!!.toString())

                    val detail = response.getJSONArray("tonase_details")

                    items.clear()

                    Log.d("tonase-data", detail.toString())

                    for (i in 0 until detail.length()) {

                        items.add(
                            Default(
                                detail.getJSONObject(i).getInt("id"),
                                detail.getJSONObject(i).getString("kilogram"),
                                detail.getJSONObject(i).getString("ekor"),
                                ""
                            )
                        )

                    }

                    mAdapter = AdapterListAnimation(applicationContext, items, animationType, false)
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
                        SweetAlertDialog(this@TonaseDetailActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@TonaseDetailActivity,
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
                        SweetAlertDialog(this@TonaseDetailActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Well Done..."
                    alertDialog.contentText = response.getString("message")
                    alertDialog.show()

                    getListTonaseD(session.idEditData)

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
                        SweetAlertDialog(this@TonaseDetailActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@TonaseDetailActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }


    private fun getDataTonaseH(idEditData: Int) {
        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = "Getting detail data"

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.tonase_header_detail+"/{id}")
            .addHeaders("Authorization", session.token)
            .addPathParameter("id",idEditData.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE
                    Log.d("tonase-detail", response!!.toString())

                    binding.etTonaseDate.setText(response.getJSONObject("tonase_header")
                        .getString("processed_at"))
                    binding.etTonasePlatNo.setText(response.getJSONObject("tonase_header")
                        .getString("plat_number"))
                    binding.etTonasePrice.setText(response.getJSONObject("tonase_header")
                        .getString("price"))

                    /*rpaIdVal = response.getJSONObject("area").getString("rpa_id")
                    binding.dropdownRPA.setText(response.getJSONObject("area").getString("rpa_name")
                            + "-" + response.getJSONObject("area").getString("rpa_address"))

                    populateRPA()*/
                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-rpa-detail", anError!!.message.toString())
                    Log.d("tonase-rpa-detail", anError.errorDetail)
                    Log.d("tonase-rpa-detail",anError.errorBody)
                    Log.d("tonase-rpa-detail", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    val alertDialog =
                        SweetAlertDialog(this@TonaseDetailActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@TonaseDetailActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

    override fun onResume() {
        super.onResume()
        getListTonaseD(session.idEditData)
    }
}
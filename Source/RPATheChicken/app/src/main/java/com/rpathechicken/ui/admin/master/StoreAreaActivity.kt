package com.rpathechicken.ui.admin.master

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.rpathechicken.R
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.ActivityStoreAreaBinding
import com.rpathechicken.databinding.ActivityStoreUserBinding
import com.rpathechicken.helpers.SessionManager
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class StoreAreaActivity : AppCompatActivity() {

    private lateinit var urlProcess: String
    private var levelIdVal: String = "0"
    private var rpaIdVal: String = "0"
    private var areaIdVal: String = "0"
    private var supIdVal: String = "0"
    private lateinit var pageName: String
    private lateinit var binding: ActivityStoreAreaBinding
    private lateinit var session: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        binding = ActivityStoreAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
        initToolbar()
    }


    private fun initComponent() {

        if(session.isCreate){
            pageName = "Create Data Area"
            urlProcess = ApiEndPoint.area_create
            populateRPA()
        }else{
            pageName = "Update Data Area"
            urlProcess = ApiEndPoint.area_update
            getDataArea(session.idEditData)
        }

        binding.btnAreaSubmit.setOnClickListener {
            when (rpaIdVal) {
                "0" -> {
                    snackBarIconInfo(it,"Please choose RPA")
                }
                else -> {
                    submitData()
                }
            }

        }

    }


    private fun initToolbar() {

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = pageName
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

    private fun submitData() {
        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = "Processing data"

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.post(urlProcess)
            .addHeaders("Authorization", session.token)
            .addBodyParameter("id", session.idEditData.toString())
            .addBodyParameter("name", binding.etAreaName.text.toString())
            .addBodyParameter("address", binding.etAreaAddress.text.toString())
            .addBodyParameter("rpa_id", rpaIdVal)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("data-rpa-detail", response!!.toString())

                    val alertDialog = SweetAlertDialog(this@StoreAreaActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Well Done..."
                    alertDialog.contentText = response.getString("message")
                    alertDialog.show()

                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("area-msg", anError!!.message.toString())
                    Log.d("area-detail", anError.errorDetail)
                    Log.d("area-body",anError.errorBody)
                    Log.d("area-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    val alertDialog =
                        SweetAlertDialog(this@StoreAreaActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreAreaActivity,
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
        val snackBarView = snackbar.view as Snackbar.SnackbarLayout
        snackBarView.setPadding(0, 0, 0, 0)
        (customView.findViewById(R.id.message) as TextView).text = msg
        (customView.findViewById(R.id.icon) as ImageView).setImageResource(R.drawable.ic_error_outline)
        snackBarView.addView(customView, 0)
        snackbar.show()
    }


    private fun populateRPA() {
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.list_rpa)
            .addHeaders("Authorization", session.token)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("area-data", response!!.toString())

                    val datas = response.getJSONArray("rpas")
                    val rpaId: MutableList<String> = ArrayList()
                    val rpas: MutableList<String> = ArrayList()

                    for (i in 0 until datas.length()) {

                        rpaId.add(datas.getJSONObject(i).getString("id"))
                        rpas.add(datas.getJSONObject(i).getString("name")+"-"
                                +datas.getJSONObject(i).getString("address") )
                    }

                    val adapterRpa: ArrayAdapter<*> =
                        ArrayAdapter<Any?>(applicationContext, android.R.layout.simple_list_item_1,
                            rpas as List<Any?>
                        )
                    binding.dropdownRPA.setAdapter(adapterRpa)
                    binding.dropdownRPA.setOnItemClickListener { adapterView, view, i, l ->
                        //Toast.makeText(applicationContext,rpaId[i], Toast.LENGTH_LONG).show()
                        rpaIdVal = rpaId[i]
                    }
                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("area-msg", anError!!.message.toString())
                    Log.d("area-detail", anError.errorDetail)
                    Log.d("area-body",anError.errorBody)
                    Log.d("area-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    val alertDialog =
                        SweetAlertDialog(this@StoreAreaActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreAreaActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

    private fun getDataArea(idEditData: Int) {
        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = "Getting detail data"

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.area_detail+"/{id}")
            .addHeaders("Authorization", session.token)
            .addPathParameter("id",idEditData.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE
                    Log.d("area-detail", response!!.toString())

                    binding.etAreaName.setText(response.getJSONObject("area").getString("name"))
                    binding.etAreaAddress.setText(response.getJSONObject("area").getString("address"))
                  
                    rpaIdVal = response.getJSONObject("area").getString("rpa_id")
                    binding.dropdownRPA.setText(response.getJSONObject("area").getString("rpa_name")
                            + "-" + response.getJSONObject("area").getString("rpa_address"))

                    populateRPA()
                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("data-rpa-detail", anError!!.message.toString())
                    Log.d("data-rpa-detail", anError.errorDetail)
                    Log.d("data-rpa-detail",anError.errorBody)
                    Log.d("data-rpa-detail", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    val alertDialog =
                        SweetAlertDialog(this@StoreAreaActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreAreaActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

}
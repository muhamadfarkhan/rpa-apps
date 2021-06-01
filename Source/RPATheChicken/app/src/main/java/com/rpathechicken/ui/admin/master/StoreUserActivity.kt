package com.rpathechicken.ui.admin.master

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.rpathechicken.R
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.ActivityStoreUserBinding
import com.rpathechicken.helpers.SessionManager
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class StoreUserActivity : AppCompatActivity() {

    private lateinit var urlProcess: String
    private var levelIdVal: String = "0"
    private var rpaIdVal: String = "0"
    private var areaIdVal: String = "0"
    private var supIdVal: String = "0"
    private lateinit var pageName: String
    private lateinit var binding: ActivityStoreUserBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        binding = ActivityStoreUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
        initToolbar()
    }

    private fun initComponent() {

        if(session.isCreate){
            pageName = "Create Data User"
            urlProcess = ApiEndPoint.user_create
            populateLevelUser()
            populateRPA()
            populateArea()
        }else{
            pageName = "Update Data User"
            urlProcess = ApiEndPoint.user_update
            getDataUser(session.idEditData)
        }

        binding.btnUserSubmit.setOnClickListener {
            when {
                levelIdVal == "0" -> {
                    snackBarIconInfo(it,"Please choose level user")
                }
                rpaIdVal == "0" -> {
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

    private fun populateArea() {
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.list_area)
            .addHeaders("Authorization", session.token)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("login-data", response!!.toString())

                    val datas = response.getJSONArray("areas")
                    val areaId: MutableList<String> = ArrayList()
                    val areas: MutableList<String> = ArrayList()

                    for (i in 0 until datas.length()) {

                        areaId.add(datas.getJSONObject(i).getString("id"))
                        areas.add(datas.getJSONObject(i).getString("name")+"-"
                                +datas.getJSONObject(i).getString("address") )
                    }

                    val adapterArea: ArrayAdapter<*> =
                        ArrayAdapter<Any?>(applicationContext, android.R.layout.simple_list_item_1,
                            areas as List<Any?>
                        )
                    binding.dropdownArea.setAdapter(adapterArea)
                    binding.dropdownArea.setOnItemClickListener { adapterView, view, i, l ->
                        //Toast.makeText(applicationContext,rpaId[i].toString(), Toast.LENGTH_LONG).show()
                        areaIdVal = areaId[i]
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
                        SweetAlertDialog(this@StoreUserActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreUserActivity,
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

    private fun getDataUser(idEditData: Int) {
        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = "Getting detail data"

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.user_detail+"/{id}")
            .addHeaders("Authorization", session.token)
            .addPathParameter("id",idEditData.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE
                    Log.d("data-rpa-detail", response!!.toString())

                    binding.etUserEmail.setText(response.getJSONObject("user").getString("email"))
                    binding.etUserName.setText(response.getJSONObject("user").getString("username"))
                    binding.etUserFullName.setText(response.getJSONObject("user").getString("name"))
                    binding.etUserPhone.setText(response.getJSONObject("user").getString("phone"))

                    levelIdVal = response.getJSONObject("level").getString("code")
                    binding.dropdownLevel.setText(response.getJSONObject("level").getString("name"))

                    rpaIdVal = response.getJSONObject("rpa").getString("id")
                    binding.dropdownRPA.setText(response.getJSONObject("rpa").getString("name")
                            + "-" + response.getJSONObject("rpa").getString("address"))

                    areaIdVal = response.getJSONObject("area").getString("id")
                    binding.dropdownArea.setText(response.getJSONObject("area").getString("name")
                            + "-" + response.getJSONObject("area").getString("address"))

                    populateLevelUser()
                    populateRPA()
                    populateArea()

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
                        SweetAlertDialog(this@StoreUserActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreUserActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
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
            .addBodyParameter("name", binding.etUserFullName.text.toString())
            .addBodyParameter("username", binding.etUserName.text.toString())
            .addBodyParameter("email", binding.etUserEmail.text.toString())
            .addBodyParameter("phone", binding.etUserPhone.text.toString())
            .addBodyParameter("level", levelIdVal)
            .addBodyParameter("rpa_id", rpaIdVal)
            .addBodyParameter("area_id", areaIdVal)
            .addBodyParameter("sup_id", "1")
            .addBodyParameter("password", "rpa123")
            .addBodyParameter("password_confirmation", "rpa123")
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("data-rpa-detail", response!!.toString())

                    val alertDialog = SweetAlertDialog(this@StoreUserActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Well Done..."
                    alertDialog.contentText = response.getString("message")
                    alertDialog.show()

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
                        SweetAlertDialog(this@StoreUserActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreUserActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
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

                    Log.d("login-data", response!!.toString())

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
                        //Toast.makeText(applicationContext,rpaId[i].toString(), Toast.LENGTH_LONG).show()
                        rpaIdVal = rpaId[i]
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
                        SweetAlertDialog(this@StoreUserActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreUserActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

    private fun populateLevelUser() {
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.list_level)
            .addHeaders("Authorization", session.token)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("login-data", response!!.toString())

                    val datas = response.getJSONArray("levels")
                    val levelId: MutableList<String> = ArrayList()
                    val levels: MutableList<String> = ArrayList()

                    for (i in 0 until datas.length()) {

                        levelId.add(datas.getJSONObject(i).getString("code"))
                        levels.add(datas.getJSONObject(i).getString("name"))
                    }

                    val adapterLevel: ArrayAdapter<*> =
                        ArrayAdapter<Any?>(applicationContext, android.R.layout.simple_list_item_1,
                            levels as List<Any?>
                        )
                    binding.dropdownLevel.setAdapter(adapterLevel)
                    binding.dropdownLevel.setOnItemClickListener { adapterView, view, i, l ->
                        //Toast.makeText(applicationContext,levelId[i].toString(), Toast.LENGTH_LONG).show()
                        levelIdVal = levelId[i]
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
                        SweetAlertDialog(this@StoreUserActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreUserActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }


}
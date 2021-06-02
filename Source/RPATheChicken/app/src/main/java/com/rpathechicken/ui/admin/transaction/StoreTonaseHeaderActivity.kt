package com.rpathechicken.ui.admin.transaction

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
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
import com.rpathechicken.R
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.ActivityStoreTonaseHeaderBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.utils.Tools
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class StoreTonaseHeaderActivity : AppCompatActivity() {

    private lateinit var urlProcess: String
    private var levelIdVal: String = "0"
    private var rpaIdVal: String = "0"
    private var areaIdVal: String = "0"
    private var supIdVal: String = "0"
    private lateinit var pageName: String
    private lateinit var binding: ActivityStoreTonaseHeaderBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        binding = ActivityStoreTonaseHeaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
        initToolbar()
    }


    private fun initComponent() {

        if(session.isCreate){
            pageName = "Create Data Tonase"
            urlProcess = ApiEndPoint.tonase_header_create
            populateRPA()
        }else{
            pageName = "Update Data Tonase"
            urlProcess = ApiEndPoint.tonase_header_update
            //getDataTonaseH(session.idEditData)
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

        binding.etTonaseDate.setOnClickListener {
            val cur_calender = Calendar.getInstance()
            val datePicker = DatePickerDialog.newInstance(
                { view, year, monthOfYear, dayOfMonth ->
                    val calendar = Calendar.getInstance()
                    calendar[Calendar.YEAR] = year
                    calendar[Calendar.MONTH] = monthOfYear
                    calendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    val date_ship_millis = calendar.timeInMillis
                    (findViewById<View>(R.id.et_tonase_date) as TextView).text =
                        Tools.getFormattedDateSimple(date_ship_millis)
                },
                cur_calender[Calendar.YEAR],
                cur_calender[Calendar.MONTH],
                cur_calender[Calendar.DAY_OF_MONTH]
            )
            //set dark light
            datePicker.isThemeDark = false
            datePicker.accentColor = resources.getColor(R.color.colorPrimary)
            datePicker.minDate = cur_calender
            datePicker.show(fragmentManager, "Datepickerdialog")
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
            .addBodyParameter("tonase_date", binding.etTonaseDate.text.toString())
            .addBodyParameter("plat_number", binding.etTonasePlatNo.text.toString())
            .addBodyParameter("rpa_id", rpaIdVal)
            .addBodyParameter("price", binding.etTonasePrice.text.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-rpa-detail", response!!.toString())

                    val alertDialog = SweetAlertDialog(this@StoreTonaseHeaderActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Well Done..."
                    alertDialog.contentText = response.getString("message")
                    alertDialog.show()

                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-msg", anError!!.message.toString())
                    Log.d("tonase-detail", anError.errorDetail)
                    Log.d("tonase-body",anError.errorBody)
                    Log.d("tonase-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    val alertDialog =
                        SweetAlertDialog(this@StoreTonaseHeaderActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreTonaseHeaderActivity,
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

                    Log.d("tonase-data", response!!.toString())

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

                    Log.d("tonase-msg", anError!!.message.toString())
                    Log.d("tonase-detail", anError.errorDetail)
                    Log.d("tonase-body",anError.errorBody)
                    Log.d("tonase-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    val alertDialog =
                        SweetAlertDialog(this@StoreTonaseHeaderActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreTonaseHeaderActivity,
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
                        SweetAlertDialog(this@StoreTonaseHeaderActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreTonaseHeaderActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

}
package com.rpathechicken.ui.admin.master

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.rpathechicken.R
import com.rpathechicken.adapter.AdapterListAnimation
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.ActivityStoreRpaBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.model.Default
import com.rpathechicken.utils.ItemAnimation
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class StoreRPAActivity : AppCompatActivity() {

    private lateinit var urlProcess: String
    private lateinit var pageName: String
    private lateinit var binding: ActivityStoreRpaBinding
    private lateinit var session: SessionManager
    private lateinit var recyclerViewUser: RecyclerView
    private lateinit var mAdapter: AdapterListAnimation
    val items = ArrayList<Default>()
    private val animationType: Int = ItemAnimation.BOTTOM_UP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        binding = ActivityStoreRpaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
        initToolbar()
    }

    private fun initComponent() {

        if(session.isCreate){
            pageName = "Create Data RPA"
            urlProcess = ApiEndPoint.rpa_create
        }else{
            pageName = "Update Data RPA"
            urlProcess = ApiEndPoint.rpa_update
            getDataRpa(session.idEditData)
        }

        binding.btnRpaSubmit.setOnClickListener {
            submitData()
        }

    }

    private fun getDataRpa(idEditData: Int) {

        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = "Getting detail data"

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.rpa_detail+"/{id}")
            .addHeaders("Authorization", session.token)
            .addPathParameter("id",idEditData.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE
                    Log.d("data-rpa-detail", response!!.toString())

                    binding.etRpaName.setText(response.getJSONObject("rpa").getString("name"))
                    binding.etRpaAddress.setText(response.getJSONObject("rpa").getString("address"))
                    binding.etRpaInitial.setText(response.getJSONObject("rpa").getString("initial"))

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
                        SweetAlertDialog(this@StoreRPAActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreRPAActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
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
            .addBodyParameter("name", binding.etRpaName.text.toString())
            .addBodyParameter("address", binding.etRpaAddress.text.toString())
            .addBodyParameter("initial", binding.etRpaInitial.text.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("data-rpa-detail", response!!.toString())

                    val alertDialog = SweetAlertDialog(this@StoreRPAActivity, SweetAlertDialog.SUCCESS_TYPE)
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
                        SweetAlertDialog(this@StoreRPAActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@StoreRPAActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }
}
package com.rpathechicken.ui.admin.transaction

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.rpathechicken.R
import com.rpathechicken.adapter.AdapterListAnimation
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.ActivityProductionDetailBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.model.Default
import com.rpathechicken.utils.ItemAnimation
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ProductionDetailActivity : AppCompatActivity() {

    private lateinit var adapterRpa: ArrayAdapter<*>
    private var rpaId: MutableList<String> = ArrayList()
    private lateinit var binding: ActivityProductionDetailBinding
    private lateinit var session: SessionManager
    private var rpaIdVal: String = "0"
    private lateinit var recyclerViewUser: RecyclerView
    private lateinit var mAdapter: AdapterListAnimation
    val items = ArrayList<Default>()
    private val animationType: Int = ItemAnimation.BOTTOM_UP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        binding = ActivityProductionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initComponent()
    }

    private fun initComponent() {

        recyclerViewUser = binding.recyclerView
        recyclerViewUser.layoutManager = LinearLayoutManager(this)
        recyclerViewUser.setHasFixedSize(true)

        binding.fabAddProduction.setOnClickListener {
            openDialogInputProd()
        }

        //getListTonaseD(session.idEditData)
        getDataTonaseH(session.idEditData)
        getMasterItem()
    }

    private fun getMasterItem() {
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.item_list)
            .addHeaders("Authorization", session.token)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-data", response!!.toString())

                    val datas = response.getJSONArray("items")

                    val rpas: MutableList<String> = ArrayList()

                    for (i in 0 until datas.length()) {

                        rpaId.add(datas.getJSONObject(i).getString("id"))
                        rpas.add(datas.getJSONObject(i).getString("initial")+"-"
                                +datas.getJSONObject(i).getString("name") )
                    }

                    adapterRpa =
                        ArrayAdapter<Any?>(applicationContext, android.R.layout.simple_list_item_1,
                            rpas as List<Any?>
                        )

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
                        SweetAlertDialog(this@ProductionDetailActivity, SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@ProductionDetailActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

    private fun initToolbar() {

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Data Production Detail"
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


    private fun openDialogInputProd() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before

        dialog.setContentView(R.layout.dialog_store_production)
        dialog.setCancelable(true)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        val itemDropdown = dialog.findViewById<View>(R.id.dropdownRPA) as MaterialAutoCompleteTextView
        itemDropdown.setAdapter(adapterRpa)
        itemDropdown.setOnItemClickListener { adapterView, view, i, l ->
            //Toast.makeText(applicationContext,rpaId[i], Toast.LENGTH_LONG).show()
            rpaIdVal = rpaId[i]
        }
        val eTonEk = dialog.findViewById<View>(R.id.et_tonase_d_ekor) as EditText
        (dialog.findViewById<View>(R.id.bt_cancel) as AppCompatButton).setOnClickListener {
            dialog.dismiss()
        }

        (dialog.findViewById<View>(R.id.bt_submit) as AppCompatButton).setOnClickListener {

            val unit = eTonEk.text.toString().trim { it <= ' ' }

            if (unit.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Please fill the blank",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                dialog.dismiss()
                storeProduction(rpaIdVal,unit)
            }

        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun storeProduction(kilo: String, ekor: String) {

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

                    binding.etTonaseTotKilo.setText(response.getJSONObject("tonase_header")
                        .getString("sum_kilo"))
                    binding.etTonaseTotEkor.setText(response.getJSONObject("tonase_header")
                        .getString("sum_ekor"))

                    binding.dropdownRPA.setText(response.getJSONObject("tonase_header").getString("rpa_name")
                            + "-" + response.getJSONObject("tonase_header").getString("rpa_address"))

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
                        SweetAlertDialog(this@ProductionDetailActivity, SweetAlertDialog.ERROR_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            this@ProductionDetailActivity,
                            R.color.colorPrimaryLight
                        )
                    )

                }

            })
    }

}
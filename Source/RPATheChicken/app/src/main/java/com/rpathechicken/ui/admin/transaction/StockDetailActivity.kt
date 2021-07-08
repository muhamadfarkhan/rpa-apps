package com.rpathechicken.ui.admin.transaction

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
import com.rpathechicken.databinding.ActivityStockDetailBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.model.Default
import com.rpathechicken.ui.admin.master.StoreAreaActivity
import com.rpathechicken.utils.ItemAnimation
import com.rpathechicken.utils.Tools
import com.rpathechicken.utils.ViewAnimation
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class StockDetailActivity : AppCompatActivity() {

    private lateinit var adapterItem: ArrayAdapter<*>
    private lateinit var adapterArea: ArrayAdapter<*>
    private lateinit var adapterSeller: ArrayAdapter<*>
    private var itemId: MutableList<String> = ArrayList()
    private var areaId: MutableList<String> = ArrayList()
    private var sellerId: MutableList<String> = ArrayList()
    private lateinit var binding: ActivityStockDetailBinding
    private lateinit var session: SessionManager
    private var itemIdVal: String = "0"
    private var areaIdVal: String = "0"
    private var sellerIdVal: String = "0"
    private lateinit var recyclerViewProd: RecyclerView
    private lateinit var mAdapter: AdapterListAnimation
    val items = ArrayList<Default>()
    private val animationType: Int = ItemAnimation.BOTTOM_UP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        binding = ActivityStockDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initComponent()
    }

    private fun initComponent() {

        recyclerViewProd = binding.recyclerView
        recyclerViewProd.layoutManager = LinearLayoutManager(this)
        recyclerViewProd.setHasFixedSize(true)

        getDataTonaseH(session.idEditData)
        getListProduction(session.idEditData)

        binding.cardViewExpand.setOnClickListener {
            toggleSectionText(binding.btToggleText)
        }

        binding.btToggleText.setOnClickListener {
            toggleSectionText(binding.btToggleText)
        }

        binding.lytExpandText.visibility = View.GONE
    }


    private fun initToolbar() {

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Data Stock Detail"
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


    private fun toggleSectionText(view: View) {
        val show: Boolean = toggleArrow(view)
        if (show) {
            ViewAnimation.expand(binding.lytExpandText, ViewAnimation.AnimListener() {
                fun onFinish() {
                    Tools.nestedScrollTo(binding.layoutUpdateTonase, binding.lytExpandText)
                }
            })
        } else {
            ViewAnimation.collapse(binding.lytExpandText)
        }
    }

    private fun toggleArrow(view: View): Boolean {
        return if (view.rotation == 0f) {
            view.animate().setDuration(200).rotation(180f)
            true
        } else {
            view.animate().setDuration(200).rotation(0f)
            false
        }
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

                    Tools.showError(this@StockDetailActivity,error)

                }

            })
    }


    private fun getListProduction(idEditData: Int) {

        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = getString(R.string.getting_data)

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.production_detail+"/{id}")
            .addHeaders("Authorization", session.token)
            .addPathParameter("id",idEditData.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-data", response!!.toString())

                    val detail = response.getJSONArray("productions")

                    items.clear()

                    Log.d("tonase-data", detail.toString())

                    for (i in 0 until detail.length()) {

                        items.add(
                            Default(
                                detail.getJSONObject(i).getInt("id"),
                                detail.getJSONObject(i).getString("item_name"),
                                detail.getJSONObject(i).getString("qty"),
                                detail.getJSONObject(i).getString("price")
                            )
                        )

                    }

                    mAdapter = AdapterListAnimation(applicationContext, items, animationType, true)
                    recyclerViewProd.adapter = mAdapter

                    mAdapter.setOnItemClickListener { _, obj, _ ->
                        //Toast.makeText(applicationContext,obj.username, Toast.LENGTH_LONG).show()
                        session.tonaseId = idEditData.toString()
                        session.itemId = obj.id.toString()
                        session.itemName = obj.name
                        startActivity(Intent(applicationContext, StockItemActivity::class.java))
                        //showAllocate(obj.id.toString(), obj.name)
                    }

                    mAdapter.isBtnRemove(false)

                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-msg", anError!!.message.toString())
                    Log.d("tonase-detail", anError.errorDetail)
                    Log.d("tonase-body", anError.errorBody)
                    Log.d("tonase-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    Tools.showError(this@StockDetailActivity,error)

                }

            })
    }


}
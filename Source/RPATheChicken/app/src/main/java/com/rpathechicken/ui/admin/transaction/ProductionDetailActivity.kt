package com.rpathechicken.ui.admin.transaction

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
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
import com.rpathechicken.utils.Tools
import com.rpathechicken.utils.ViewAnimation
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ProductionDetailActivity : AppCompatActivity() {

    private lateinit var adapterItem: ArrayAdapter<*>
    private var itemId: MutableList<String> = ArrayList()
    private lateinit var binding: ActivityProductionDetailBinding
    private lateinit var session: SessionManager
    private var itemIdVal: String = "0"
    private lateinit var recyclerViewProd: RecyclerView
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

        recyclerViewProd = binding.recyclerView
        recyclerViewProd.layoutManager = LinearLayoutManager(this)
        recyclerViewProd.setHasFixedSize(true)

        binding.fabAddProduction.setOnClickListener {
            openDialogInputProd()
        }

        getListProduction(session.idEditData)
        getDataTonaseH(session.idEditData)
        getMasterItem()

        binding.cardViewExpand.setOnClickListener {
            toggleSectionText(binding.btToggleText)
        }

        binding.btToggleText.setOnClickListener {
            toggleSectionText(binding.btToggleText)
        }

        binding.lytExpandText.visibility = GONE
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
                        //session.idEditData = obj.id
                        //session.isCreate = false
                        //startActivity(Intent(applicationContext, TonaseDetailActivity::class.java))
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

                    Tools.showError(this@ProductionDetailActivity,error)

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
                deleteProduction(id)
            }
            .setCancelButton(
                "Cancel"
            ) { sDialog -> sDialog.dismissWithAnimation() }
            .show()
    }

    private fun deleteProduction(id: Int) {
        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = getString(R.string.delete_data)

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.post(ApiEndPoint.production_destroy)
            .addHeaders("Authorization", session.token)
            .addBodyParameter("id", id.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-detail", response!!.toString())

                    Tools.showSuccess(this@ProductionDetailActivity,response.getString("message"))

                    getListProduction(session.idEditData)
                    getDataTonaseH(session.idEditData)

                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-msg", anError!!.message.toString())
                    Log.d("tonase-detail", anError.errorDetail)
                    Log.d("tonase-body", anError.errorBody)
                    Log.d("tonase-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    Tools.showError(this@ProductionDetailActivity,error)

                }

            })
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

                    val items: MutableList<String> = ArrayList()

                    for (i in 0 until datas.length()) {

                        itemId.add(datas.getJSONObject(i).getString("id"))
                        items.add(datas.getJSONObject(i).getString("initial")+"-"
                                +datas.getJSONObject(i).getString("name") )
                    }

                    adapterItem =
                        ArrayAdapter<Any?>(applicationContext, android.R.layout.simple_list_item_1,
                            items as List<Any?>
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

                    Tools.showError(this@ProductionDetailActivity,error)

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

        val itemDropdown = dialog.findViewById<View>(R.id.dropdownItem) as MaterialAutoCompleteTextView
        itemDropdown.setAdapter(adapterItem)
        itemDropdown.setOnItemClickListener { adapterView, view, i, l ->
            //Toast.makeText(applicationContext,rpaId[i], Toast.LENGTH_LONG).show()
            itemIdVal = itemId[i]
        }
        val eProdUnit = dialog.findViewById<View>(R.id.et_unit_prod) as EditText
        val eCapitalPrice = dialog.findViewById<View>(R.id.et_capital_price) as EditText
        val eSellPrice = dialog.findViewById<View>(R.id.et_sell_price) as EditText
        (dialog.findViewById<View>(R.id.bt_cancel) as AppCompatButton).setOnClickListener {
            dialog.dismiss()
        }

        (dialog.findViewById<View>(R.id.bt_submit) as AppCompatButton).setOnClickListener {

            val unit = eProdUnit.text.toString().trim { it <= ' ' }
            val capitalPrice = eCapitalPrice.text.toString().trim { it <= ' ' }
            val sellPrice = eSellPrice.text.toString().trim { it <= ' ' }

            if (unit.isEmpty() || capitalPrice.isEmpty() || sellPrice.isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Please fill the blank",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                dialog.dismiss()
                storeProduction(itemIdVal,unit,capitalPrice,sellPrice)
            }

        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun storeProduction(itemIdVal: String, unit: String, capitalPrice: String, sellPrice: String) {
        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = "Processing data"

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.post(ApiEndPoint.production_create)
            .addHeaders("Authorization", session.token)
            .addBodyParameter("qty", unit)
            .addBodyParameter("item_id", itemIdVal)
            .addBodyParameter("capital_price", capitalPrice)
            .addBodyParameter("sell_price", sellPrice)
            .addBodyParameter("tonase_id", session.idEditData.toString())
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-rpa-detail", response!!.toString())

                    Tools.showSuccess(this@ProductionDetailActivity,response.getString("message"))

                    getListProduction(session.idEditData)
                    
                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("tonase-msg", anError!!.message.toString())
                    Log.d("tonase-detail", anError.errorDetail)
                    Log.d("tonase-body",anError.errorBody)
                    Log.d("tonase-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    Tools.showError(this@ProductionDetailActivity,error)

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

                    Tools.showError(this@ProductionDetailActivity,error)

                }

            })
    }

}
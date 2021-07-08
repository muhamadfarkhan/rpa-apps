package com.rpathechicken.ui.admin.transaction

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.rpathechicken.R
import com.rpathechicken.adapter.AdapterListAnimation
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.ActivityStockItemBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.model.Default
import com.rpathechicken.utils.ItemAnimation
import com.rpathechicken.utils.Tools
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class StockItemActivity : AppCompatActivity() {

    private lateinit var adapterItem: ArrayAdapter<*>
    private lateinit var adapterArea: ArrayAdapter<*>
    private lateinit var adapterSeller: ArrayAdapter<*>
    private var itemId: MutableList<String> = ArrayList()
    private var areaId: MutableList<String> = ArrayList()
    private var sellerId: MutableList<String> = ArrayList()
    private lateinit var binding: ActivityStockItemBinding
    private lateinit var session: SessionManager
    private var itemIdVal: String = "0"
    private var areaIdVal: String = "0"
    private var itemName: String = "0"
    private var sellerIdVal: String = "0"
    private lateinit var recyclerViewProd: RecyclerView
    private lateinit var mAdapter: AdapterListAnimation
    val items = ArrayList<Default>()
    private val animationType: Int = ItemAnimation.BOTTOM_UP
    private lateinit var tonaseIdProcess: String
    private lateinit var itemIdProcess: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        tonaseIdProcess = session.tonaseId.toString()
        itemIdProcess = session.itemId.toString()

        binding = ActivityStockItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initComponent()

    }

    private fun initComponent() {
        getListArea()
        getListSeller()
        getListStockItem()

        itemName = session.itemName.toString()
        binding.etItemName.setText(session.itemName.toString())

        binding.fabAllocateItem.setOnClickListener {
            showAllocate(tonaseIdProcess, itemIdProcess)
        }
    }


    private fun initToolbar() {

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Data Stock Item"
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


    private fun showAllocate(idTonase: String, idItem: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before

        dialog.setContentView(R.layout.dialog_allocate_item)
        dialog.setCancelable(true)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT

        val itemDropdown = dialog.findViewById<View>(R.id.dropdownArea) as MaterialAutoCompleteTextView
        itemDropdown.setAdapter(adapterArea)
        itemDropdown.setOnItemClickListener { adapterView, view, i, l ->
            //Toast.makeText(applicationContext,rpaId[i], Toast.LENGTH_LONG).show()
            areaIdVal = areaId[i]
        }

        val sellerDropdown = dialog.findViewById<View>(R.id.dropdownSeller) as MaterialAutoCompleteTextView
        sellerDropdown.setAdapter(adapterSeller)
        sellerDropdown.setOnItemClickListener { adapterView, view, i, l ->
            //Toast.makeText(applicationContext,rpaId[i], Toast.LENGTH_LONG).show()
            sellerIdVal = sellerId[i]
        }

        val eProdUnit = dialog.findViewById<View>(R.id.et_unit_prod) as EditText
        val etItemName = dialog.findViewById<View>(R.id.et_item_name) as EditText

        etItemName.setText(itemName)

        (dialog.findViewById<View>(R.id.bt_cancel) as AppCompatButton).setOnClickListener {
            dialog.dismiss()
        }

        (dialog.findViewById<View>(R.id.bt_submit) as AppCompatButton).setOnClickListener {

            val unit = eProdUnit.text.toString().trim { it <= ' ' }

            if (unit.isEmpty()){
                Toast.makeText(
                    applicationContext,
                    "Please fill the blank",
                    Toast.LENGTH_SHORT
                ).show()
            }else{
                dialog.dismiss()
                allocateItem(areaIdVal,unit)
            }

        }

        dialog.show()
        dialog.window!!.attributes = lp
    }

    private fun allocateItem(itemIdVal: String, unit: String) {

        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = getString(R.string.getting_data)

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.post(ApiEndPoint.stock_allocate)
            .addHeaders("Authorization", session.token)
            .addBodyParameter("tonase_id", tonaseIdProcess)
            .addBodyParameter("item_id", itemIdProcess)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("stockitem-allocate-msg", anError!!.message.toString())
                    Log.d("stockitem-allocate-detail", anError.errorDetail)
                    Log.d("stockitem-allocate-body", anError.errorBody)
                    Log.d("stockitem-allocate-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    Tools.showError(this@StockItemActivity,error)

                }

            })
    }

    private fun getListArea() {

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

                    val areas = response!!.getJSONArray("areas")

                    val items: MutableList<String> = ArrayList()

                    for (i in 0 until areas.length()) {

                        areaId.add(areas.getJSONObject(i).getString("id"))
                        items.add(areas.getJSONObject(i).getString("name")+"-"
                                +areas.getJSONObject(i).getString("address") )
                    }

                    adapterArea =
                        ArrayAdapter<Any?>(applicationContext, android.R.layout.simple_list_item_1,
                            items as List<Any?>
                        )

                }

                override fun onError(anError: ANError?) {


                }

            })
    }


    private fun getListSeller() {

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.seller_list)
            .addHeaders("Authorization", session.token)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    val sellers = response!!.getJSONArray("sellers")

                    Log.d("stockitem", sellers.toString())

                    val items: MutableList<String> = ArrayList()

                    for (i in 0 until sellers.length()) {

                        sellerId.add(sellers.getJSONObject(i).getString("id"))
                        items.add(sellers.getJSONObject(i).getString("name"))
                    }

                    adapterSeller =
                        ArrayAdapter<Any?>(applicationContext, android.R.layout.simple_list_item_1,
                            items as List<Any?>
                        )

                }

                override fun onError(anError: ANError?) {


                }

            })
    }

    private fun getListStockItem(){
        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = getString(R.string.getting_data)

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.post(ApiEndPoint.stock_detail_item)
            .addHeaders("Authorization", session.token)
            .addBodyParameter("tonase_id", tonaseIdProcess)
            .addBodyParameter("item_id", itemIdProcess)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("stockitem-data", response!!.toString())

                    val stocks = response.getJSONArray("stocks")

                    items.clear()

                    Log.d("stockitem-data", stocks.toString())

                    for (i in 0 until stocks.length()) {

                        items.add(
                            Default(
                                stocks.getJSONObject(i).getInt("id"),
                                stocks.getJSONObject(i).getString("seller_id"),
                                stocks.getJSONObject(i).getString("seller_id"),
                                stocks.getJSONObject(i).getString("seller_id")
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
                        //confirmDelete(obj.id)
                    }
                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("stockitem-msg", anError!!.message.toString())
                    Log.d("stockitem-detail", anError.errorDetail)
                    Log.d("stockitem-body", anError.errorBody)
                    Log.d("stockitem-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    Tools.showError(this@StockItemActivity,error)

                }

            })
    }
}
package com.rpathechicken.ui.admin.transaction

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.rpathechicken.R
import com.rpathechicken.adapter.AdapterListTonase
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.ActivityStockBinding
import com.rpathechicken.databinding.ActivityTonaseHeaderBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.model.Tonase
import com.rpathechicken.utils.ItemAnimation
import com.rpathechicken.utils.Tools
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class StockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStockBinding
    private lateinit var session: SessionManager
    private lateinit var recyclerViewUser: RecyclerView
    private lateinit var mAdapter: AdapterListTonase
    val items = ArrayList<Tonase>()
    private val animationType: Int = ItemAnimation.BOTTOM_UP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        binding = ActivityStockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolbar()
        initComponent()
        getListStockH()
    }

    private fun initComponent() {

        recyclerViewUser = binding.recyclerView
        recyclerViewUser.layoutManager = LinearLayoutManager(this)
        recyclerViewUser.setHasFixedSize(true)

    }

    private fun initToolbar() {

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Data Stock Production"
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


    private fun getListStockH() {

        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE
        binding.layoutProgress.textLoading.text = getString(R.string.getting_data)

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.stock_header_list)
            .addHeaders("Authorization", session.token)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("stocks-data", response!!.toString())

                    val user = response.getJSONArray("stocks")

                    items.clear()

                    Log.d("stocks-data", user.toString())

                    for (i in 0 until user.length()) {

                        val rpa = user.getJSONObject(i).getJSONObject("rpa")

                        items.add(
                            Tonase(
                                user.getJSONObject(i).getInt("id"),
                                rpa.getString("name"),
                                user.getJSONObject(i).getString("processed_at"),
                                user.getJSONObject(i).getString("price"),
                                user.getJSONObject(i).getString("plat_number")
                            )
                        )

                    }


                    mAdapter = AdapterListTonase(applicationContext, items, animationType)
                    recyclerViewUser.adapter = mAdapter

                    mAdapter.setOnItemClickListener { _, obj, _ ->
                        //Toast.makeText(applicationContext,obj.username, Toast.LENGTH_LONG).show()
                        session.idEditData = obj.id
                        session.isCreate = false
                        startActivity(Intent(applicationContext, StockDetailActivity::class.java))
                    }

                }

                override fun onError(anError: ANError?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    Log.d("stocks-msg", anError!!.message.toString())
                    Log.d("stocks-detail", anError.errorDetail)
                    Log.d("stocks-body", anError.errorBody)
                    Log.d("stocks-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    Tools.showError(this@StockActivity,error)

                }

            })
    }

}
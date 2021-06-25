package com.rpathechicken.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.rpathechicken.R
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.FragmentHomeBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.ui.admin.master.MasterAreaActivity
import com.rpathechicken.ui.admin.master.MasterItemActivity
import com.rpathechicken.ui.admin.master.MasterRPAActivity
import com.rpathechicken.ui.admin.master.MasterUserActivity
import com.rpathechicken.ui.admin.transaction.ProductionActivity
import com.rpathechicken.ui.admin.transaction.TonaseHeaderActivity
import com.rpathechicken.utils.Tools
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private lateinit var session: SessionManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        session = SessionManager(requireContext())

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initButton()
        initComponen()

        checkProfile()

        return root
    }

    private fun checkProfile() {

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.get(ApiEndPoint.profile)
            .addHeaders("Authorization", session.token)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    Log.d("login-data", response!!.toString())

                    val user = response.getJSONObject("user")

                    session.username = user.getString("username")
                    session.fullname = user.getString("name")
                    session.phone = user.getString("phone")


                }

                override fun onError(anError: ANError?) {

                    Log.d("login-msg", anError!!.message.toString())
                    Log.d("login-detail", anError.errorDetail)
                    Log.d("login-body",anError.errorBody)
                    Log.d("login-code", anError.errorCode.toString())

                    val errorBody = JSONObject(anError.errorBody)

                    val error = errorBody.getString("message")

                    val alertDialog =
                        SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                    alertDialog.titleText = "Oops..."
                    alertDialog.contentText = error
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorPrimaryLight
                        )
                    )

                    if(error.contains("autho")){
                        alertDialog.setConfirmClickListener {
                            session.logout()
                            activity!!.finish()
                        }
                    }


                }

            })
    }

    private fun initComponen() {
        binding.toolbar.title = session.fullname
        binding.toolbar.subtitle = getString(R.string.welcome_to)+" "+getString(R.string.app_name)
    }

    private fun initButton() {
        binding.btnMasterUser.setOnClickListener {
            startActivity(Intent(context, MasterUserActivity::class.java))
        }
        binding.btnMasterRpa.setOnClickListener {
            startActivity(Intent(context, MasterRPAActivity::class.java))
        }
        binding.btnMasterArea.setOnClickListener {
            startActivity(Intent(context, MasterAreaActivity::class.java))
        }
        binding.btnMasterItem.setOnClickListener {
            startActivity(Intent(context, MasterItemActivity::class.java))
        }
        binding.btnTransTonase.setOnClickListener {
            startActivity(Intent(context, TonaseHeaderActivity::class.java))
        }
        binding.btnTransProduction.setOnClickListener {
            startActivity(Intent(context, ProductionActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
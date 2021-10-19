package com.rpathechicken.ui.profile

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.rpathechicken.MainActivity
import com.rpathechicken.R
import com.rpathechicken.api.ApiEndPoint
import com.rpathechicken.databinding.FragmentProfileBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.ui.LoginActivity
import com.rpathechicken.ui.profile.detail.PersonalDetailActivity
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class ProfileFragment : Fragment() {

    private lateinit var notificationsViewModel: ProfileViewModel
    private var _binding: FragmentProfileBinding? = null
    private lateinit var session: SessionManager


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        notificationsViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        session = SessionManager(requireContext())

        initToolbar()
        initComponent()
        initButton()

        return binding.root
    }

    private fun initButton() {
        binding.layoutProfileMenu.personalDetail.setOnClickListener {
            startActivity(Intent(context, PersonalDetailActivity::class.java))
        }

        binding.appVersion.text = getString(R.string.app_version)

        binding.layoutProfileMenu.aboutApp.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before

            dialog.setContentView(R.layout.dialog_about)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(true)
            dialog.findViewById<View>(R.id.bt_close).setOnClickListener {
                dialog.dismiss()
            }
            dialog.show()
        }

        binding.layoutProfileMenu.logoutApp.setOnClickListener {
            val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Are you sure ?")
            builder.setPositiveButton("Logout"
            ) { _, _ ->
                session.logout()
                activity?.finish()
                startActivity(Intent(context, LoginActivity::class.java))
            }
            builder.setNegativeButton("cancel", null)
            builder.show()
        }

        binding.layoutProfileMenu.changePwd.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // before

            dialog.setContentView(R.layout.dialog_change_pwd)
            dialog.setCancelable(true)

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(dialog.window!!.attributes)
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT

            val eTOldPwd = dialog.findViewById<View>(R.id.et_old_pwd) as EditText
            val eTNewPwd = dialog.findViewById<View>(R.id.et_new_pwd) as EditText
            (dialog.findViewById<View>(R.id.bt_cancel) as AppCompatButton).setOnClickListener {
                dialog.dismiss()
            }

            (dialog.findViewById<View>(R.id.bt_submit) as AppCompatButton).setOnClickListener {

                    val oldPwd = eTOldPwd.text.toString().trim { it <= ' ' }
                    val newPwd = eTNewPwd.text.toString().trim { it <= ' ' }

                    if (oldPwd.isEmpty() or newPwd.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Please fill the blank",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        dialog.dismiss()
                        processChangePwd(oldPwd,newPwd)
                    }

                }

            dialog.show()
            dialog.window!!.attributes = lp
        }
    }

    private fun processChangePwd(oldPwd: String, newPwd: String) {
        binding.layoutProgress.textLoading.text = getString(R.string.changing_password)
        binding.layoutProgress.progressOverlay.visibility = View.VISIBLE

        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()

        AndroidNetworking.post(ApiEndPoint.change_pwd)
            .addHeaders("Authorization", session.token)
            .addBodyParameter("old_password",oldPwd)
            .addBodyParameter("new_password",newPwd)
            .setPriority(Priority.MEDIUM)
            .setOkHttpClient(okHttpClient)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {

                    binding.layoutProgress.progressOverlay.visibility = View.GONE

                    val msg = response!!.getString("message").toString()

                    val alertDialog =
                        SweetAlertDialog(requireContext(), SweetAlertDialog.NORMAL_TYPE)
                    alertDialog.titleText = "Well done!"
                    alertDialog.contentText = msg
                    alertDialog.show()

                    val btn: Button = alertDialog.findViewById<View>(R.id.confirm_button) as Button
                    btn.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorPrimaryLight
                        )
                    )

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

                }

            })
    }

    private fun initToolbar() {
        val toolbar: Toolbar = binding.toolbar

        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.title = session.fullname
    }

    private fun initComponent() {
        binding.layoutProgress.progressOverlay.visibility = View.GONE

        val image = binding.image
        val collapsingToolbar = binding.collapsingToolbar
        binding.appBarLayout.addOnOffsetChangedListener(
            OnOffsetChangedListener { _, verticalOffset ->
                val minHeight = ViewCompat.getMinimumHeight(collapsingToolbar) * 2
                val scale = (minHeight + verticalOffset).toFloat() / minHeight
                image.scaleX = (if (scale >= 0) scale else 0F)
                image.scaleY = (if (scale >= 0) scale else 0F)
            })

        binding.userMail.text = session.email
        binding.userPhone.text = session.phone
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
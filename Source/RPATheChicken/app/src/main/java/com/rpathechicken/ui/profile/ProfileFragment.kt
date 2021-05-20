package com.rpathechicken.ui.profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.rpathechicken.R
import com.rpathechicken.databinding.FragmentProfileBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.ui.LoginActivity
import com.rpathechicken.ui.profile.detail.PersonalDetailActivity


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
    }

    private fun initToolbar() {
        val toolbar: Toolbar = binding.toolbar

        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.title = "Evans Collins"
    }

    private fun initComponent() {
        val image = binding.image
        val collapsingToolbar = binding.collapsingToolbar
        binding.appBarLayout.addOnOffsetChangedListener(
            OnOffsetChangedListener { _, verticalOffset ->
                val minHeight = ViewCompat.getMinimumHeight(collapsingToolbar) * 2
                val scale = (minHeight + verticalOffset).toFloat() / minHeight
                image.scaleX = (if (scale >= 0) scale else 0F)
                image.scaleY = (if (scale >= 0) scale else 0F)
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.rpathechicken.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rpathechicken.R
import com.rpathechicken.databinding.FragmentHomeBinding
import com.rpathechicken.helpers.SessionManager
import com.rpathechicken.ui.admin.master.MasterAreaActivity
import com.rpathechicken.ui.admin.master.MasterItemActivity
import com.rpathechicken.ui.admin.master.MasterRPAActivity
import com.rpathechicken.ui.admin.master.MasterUserActivity

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

        return root
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
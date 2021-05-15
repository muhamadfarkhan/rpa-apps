package com.rpathechicken.ui.profile

import android.R
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.mikhaellopez.circularimageview.CircularImageView
import com.rpathechicken.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    private lateinit var notificationsViewModel: ProfileViewModel
    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)

        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        initToolbar()
        initComponent()

        return binding.root
    }

    private fun initToolbar() {
        val toolbar: Toolbar = binding.toolbar

        val activity = activity as AppCompatActivity
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.title = "Evans Collins"
    }

    private fun initComponent() {
        val image = binding.image
        val collapsing_toolbar = binding.collapsingToolbar
        binding.appBarLayout.addOnOffsetChangedListener(
            OnOffsetChangedListener { appBarLayout, verticalOffset ->
                val min_height = ViewCompat.getMinimumHeight(collapsing_toolbar) * 2
                val scale = (min_height + verticalOffset).toFloat() / min_height
                image.scaleX = (if (scale >= 0) scale else 0F)
                image.scaleY = (if (scale >= 0) scale else 0F)
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
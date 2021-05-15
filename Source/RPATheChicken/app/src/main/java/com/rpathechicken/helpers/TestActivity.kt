package com.rpathechicken.helpers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rpathechicken.databinding.ActivityMainBinding
import com.rpathechicken.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.testing.text = "update"
    }
}
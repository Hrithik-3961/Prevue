package com.hrithik.prevue

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.PointerIcon
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.hrithik.prevue.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

        }
    }
}
package com.prosthetik.beattracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

        val nameTextView = findViewById<TextView>(R.id.user_name)
        nameTextView.text = intent.getStringExtra("name")
    }
}
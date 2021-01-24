package com.prosthetik.beattracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.switchmaterial.SwitchMaterial

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

        val nameTextView = findViewById<TextView>(R.id.user_name)
        nameTextView.text = intent.getStringExtra("name")

        val bottomSheetLayout = findViewById<ConstraintLayout>(R.id.bottomFragment)
        val bottomSheet = BottomSheetBehavior.from(bottomSheetLayout)

        bottomSheet.isHideable = false
        bottomSheet.peekHeight = 240
        bottomSheet.isDraggable = true

        findViewById<TextView>(R.id.fragment_email).text = "for " + intent.getStringExtra("email")

        val liveButton = findViewById<SwitchMaterial>(R.id.shareLocation)
        liveButton.setOnCheckedChangeListener { _, isChecked: Boolean ->
            val msg: String = when(isChecked){
                true -> "Going live..."
                false -> "You're off the radar."
            }

            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }
}
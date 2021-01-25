package com.prosthetik.beattracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.switchmaterial.SwitchMaterial

class DashboardActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mLocationManager: LocationManager

    companion object{
        @JvmStatic val locationPermissionCode: Int = 0x3f
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isBuildingsEnabled = true

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        }

        mMap.isMyLocationEnabled = true

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(49.246292, -123.116226)
//        mMap.addMarker(MarkerOptions()
//                .position(sydney)
//                .title("Marker in Sydney"))
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, mMap.maxZoomLevel))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

        //manage Map Fragment here
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        //end map related tweaks

        //name to display below "Welcome," message
        val nameTextView = findViewById<TextView>(R.id.user_name)
        nameTextView.text = intent.getStringExtra("name")

        //manage bottomSheet Fragment here
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

        //end bottomSheet related tweaks
    }
}
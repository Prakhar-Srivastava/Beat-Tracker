package com.prosthetik.beattracker

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.DashPathEffect
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.switchmaterial.SwitchMaterial

class DashboardActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var locationProvider: FusedLocationProviderClient

    companion object{
        @JvmStatic val locationPermissionCode: Int = 0x3f
        @JvmStatic val locationDeniedToken = CancellationTokenSource()
        @JvmStatic val GOING_LIVE: String = "Going live..."
        @JvmStatic val OFF_THE_RADAR: String = "You're off the radar."
    }

    private fun isLocationServiceRunning(): Boolean {
        val mgr = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?

        val nServices: Int? = (
                mgr?.getRunningServices(Int.MAX_VALUE)?.filter {
                    LocationForegroundService::class.java.name == it.service.className
                    && it.foreground
                })?.size

        return nServices != null && nServices > 0
    }

    private fun goLive(): String {
        if(!isLocationServiceRunning()){
            val launchService = Intent(
                applicationContext,
                LocationForegroundService::class.java
            )
            launchService.action = LocationForegroundService.ACTION_START_LOCATION_SERVICE
            startService(launchService)
            return GOING_LIVE
        }
        return ""
    }

    private fun stopBroadcasting(): String {
        if(isLocationServiceRunning()){
            val launchService = Intent(
                applicationContext,
                LocationForegroundService::class.java
            )
            launchService.action = LocationForegroundService.ACTION_STOP_LOCATION_SERVICE
            startService(launchService)
            return OFF_THE_RADAR
        }
        return ""
    }

    private fun centerMap() {
        //wait until the location provider is initialized
        while (!this::locationProvider.isInitialized);
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationProvider.getCurrentLocation(
                LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,
                locationDeniedToken.token
            ).addOnSuccessListener { location: Location ->
                val me = LatLng(location.latitude, location.longitude)
                val lookHere = CameraPosition.Builder()
                    .target(me)
                    .zoom(17f)
                    .bearing(90f)
                    .tilt(30f)
                    .build()

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(lookHere))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isBuildingsEnabled = true

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {
            mMap.isMyLocationEnabled = true
            centerMap()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                if(this::mMap.isInitialized) centerMap()
            }
            else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard2)

        //initialize location provider
        locationProvider = LocationServices.getFusedLocationProviderClient(this)

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
        liveButton.isChecked = isLocationServiceRunning()
        liveButton.setOnCheckedChangeListener { _, isChecked: Boolean ->
            val msg: String = when(isChecked){
                true -> goLive()
                false -> stopBroadcasting()
            }

            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }

        //end bottomSheet related tweaks
    }
}
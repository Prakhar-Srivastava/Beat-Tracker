package com.prosthetik.beattracker

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.prosthetik.beattracker.datamodels.CurrentLocation

class LocationForegroundService : Service() {
    private lateinit var database: FirebaseDatabase
    private lateinit var ref: DatabaseReference

    companion object{
        @JvmStatic val LOCATION_SERVICE_ID: Int = 0x42
        @JvmStatic val ACTION_START_LOCATION_SERVICE: String = "startLocationService"
        @JvmStatic val ACTION_STOP_LOCATION_SERVICE: String = "stopLocationService"
        @JvmStatic val channelId: String = "location_notification_channel"
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            if(locationResult?.lastLocation != null){
                val lat: Double = locationResult.lastLocation.latitude
                val lng: Double = locationResult.lastLocation.longitude
                val model = CurrentLocation(System.currentTimeMillis(), lat, lng)
                val id: String? = ref.push().key
                ref.child(id!!).setValue(model)

                Log.d("LOCATION_UPDATE", "$lat°N $lng°E.")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action){
            ACTION_START_LOCATION_SERVICE -> {
                database = FirebaseDatabase.getInstance()
                ref = intent.getStringExtra("email")?.let { database.getReference(it) }!!
                startLocationService()
            }
            ACTION_STOP_LOCATION_SERVICE -> stopLocationService()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startLocationService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val result = Intent()
        val pending = PendingIntent.getActivity(
            applicationContext,
            0,
            result,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notifBuilder = NotificationCompat.Builder(
            applicationContext,
            channelId
        )

        //configure notification
        notifBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
        notifBuilder.setContentTitle("Live Chasing Beat")
        notifBuilder.setDefaults(NotificationCompat.DEFAULT_ALL)
        notifBuilder.setContentText("Broadcasting live location")
        notifBuilder.setContentIntent(pending)
        notifBuilder.setAutoCancel(false)
        notifBuilder.priority = NotificationCompat.PRIORITY_MAX

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(notificationManager.getNotificationChannel(channelId) == null){
                val notifChannel = NotificationChannel(
                    channelId,
                    "Live Location Broadcast Service",
                    NotificationManager.IMPORTANCE_HIGH
                )

                notifChannel.description = "This channel is used by the Live Location Broadcast Service"
                notificationManager.createNotificationChannel(notifChannel)
            }
        }

        val locationRequest = LocationRequest()
        locationRequest.interval = 4000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(this)
                .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

            startForeground(LOCATION_SERVICE_ID, notifBuilder.build())
        }
    }

    private fun stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(locationCallback)

        stopForeground(true)
        stopSelf()
    }
}
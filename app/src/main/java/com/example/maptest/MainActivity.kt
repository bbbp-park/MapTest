package com.example.maptest

import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.maptest.locationapi.service.LocationService
import com.example.maptest.permission.PermissionManager
import com.google.android.gms.location.LocationRequest
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    lateinit var textViewState: TextView
    lateinit var textViewLocation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewState = findViewById(R.id.textViewState)
        textViewLocation = findViewById(R.id.textViewLocation)

        findViewById<Button>(R.id.buttonStart).setOnClickListener {
            // check permission here

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PermissionManager.requestPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) {
                    requestLocation()
                }
            } else {
                PermissionManager.requestPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                ) {
                    requestLocation()
                }
            }
        }

        findViewById<Button>(R.id.buttonStop).setOnClickListener {
            com.example.maptest.locationapi.LocationManager.stop(this)
        }

        textViewState.text = "is location service running ? ${LocationService.isLocationServiceRunning}"

        if (LocationService.isLocationServiceRunning) {
            requestLocation()
        }
    }

    private fun requestLocation() {

        com.example.maptest.locationapi.LocationManager.Builder
            .setInterval(4000)
            .setFastestInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .create(this)
            .request(true) { latitude, longitude ->

                val locationString = "$latitude\n$longitude"
                textViewLocation.text = locationString
                textViewState.text = "is location service running ? ${LocationService.isLocationServiceRunning}"

            }


//        com.example.maptest.locationapi.LocationManager.Builder
//            .setInterval(4000)
//            .setFastestInterval(1000)
//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//            .create(this)
//            .request(false) { latitude, longitude ->
//
//                val locationString = "$latitude\n$longitude"
//                textViewLocation.text = locationString
//
//                // if you want to request location just once, ths
//                com.example.maptest.locationapi.LocationManager.stop(this)
//            }

        // interval, priority has default value, so I can use LocationManager.Builder without setInterval, setPriority
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        PermissionManager.onActivityResult(resultCode, resultCode)
        super.onActivityResult(requestCode, resultCode, data)
    }
}
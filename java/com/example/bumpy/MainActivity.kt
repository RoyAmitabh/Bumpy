package com.example.bumpy


import android.view.View
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() ,  SensorEventListener, LocationListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var locationManager: LocationManager
    private lateinit var locationView: TextView
    private lateinit var zAccelerationTextView: TextView
    private var lastZAcceleration: Float = 0.0f
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        zAccelerationTextView = findViewById(R.id.zAccelerationTextView)
        locationView = findViewById(R.id.locationView)

        // Initialize sensor manager and accelerometer
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Initialize location manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Request location permission if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResume() {
        super.onResume()
        // Register accelerometer listener when the activity resumes
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        // Request location updates
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000,
            1.0f,
            this
        )
    }

    override fun onPause() {
        super.onPause()
        // Unregister the accelerometer listener and stop location updates when the activity is paused
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Handle accelerometer data here to detect bumps
        // You need to implement the bump detection logic based on the accelerometer readings
        // When a bump is detected, call a method to save the GPS location
        // For simplicity, I'm assuming a bump is detected when the z-axis (vertical) acceleration is higher than a threshold.

        val zAcceleration = event.values[2]
        lastZAcceleration = zAcceleration
        // Set a threshold for bump detection (you might need to fine-tune this value)
        val bumpThreshold = 9.8f

        if (zAcceleration > bumpThreshold) {
            saveBumpLocation()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }

    override fun onLocationChanged(location: Location) {
        // This method is called whenever the GPS location is updated
        // You can save the location data in a local database or send it to a cloud service
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Do nothing
    }

    override fun onProviderEnabled(provider: String) {
        // Do nothing
    }

    override fun onProviderDisabled(provider: String) {
        // Do nothing
    }

    private fun saveBumpLocation() {
        // Implement your logic here to save the GPS location when a bump is detected

        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // If location permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        // If location permission is granted, retrieve the GPS location
        val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        // Check if the location is not null
        if (location != null) {
            // Retrieve the latitude and longitude
            val latitude = location.latitude
            val longitude = location.longitude
            // Retrieve the Z-axis acceleration value
            val zAcceleration = lastZAcceleration
            // Update the TextViews with the location data and Z-axis acceleration value, and make them visible
            val zAccelerationText = "Z-Axis Acceleration: $zAcceleration"
            zAccelerationTextView.text = zAccelerationText
            zAccelerationTextView.visibility = View.VISIBLE

            val locationText = "Latitude: $latitude, Longitude: $longitude"
            this.locationView.text = locationText
            this.locationView.visibility = View.VISIBLE

            // Use the handler to hide the TextView after 3 seconds
            handler.postDelayed({
                zAccelerationTextView.visibility = View.GONE
                locationView.visibility = View.GONE
            }, 3000)
        } else {
            // Location is null, handle the case when location is not available
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
        }
    }



}

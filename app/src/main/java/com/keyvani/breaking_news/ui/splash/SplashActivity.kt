package com.keyvani.breaking_news.ui.splash

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.Window
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.keyvani.breaking_news.R
import com.keyvani.breaking_news.databinding.ActivitySplashBinding
import com.keyvani.breaking_news.ui.MainActivity
import com.keyvani.breaking_news.utils.Constants
import org.json.JSONObject
import timber.log.Timber
import java.io.InputStream
import java.util.*

//got help from "Wandering in Google maps with Kotlin" on Udacity -> ADVANCED ANDROID APPS WITH KOTLIN - PART 2

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationRequestId = 100
    private var googleApiClient: GoogleApiClient? = null
    private val requestLocation = 199


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

    }

    private fun getLocation(): Boolean {

        if (checkForLocationPermission()) {
            if (isLocationEnable()) {
                updateLocation()

            } else {
                locationEnabling()
                updateLocation()


            }
        } else {
            askLocationPermission()
        }
        return true
    }

    /**
    This code is checking whether location services are enabled on the device by checking if either the GPS
    or network provider is enabled on the device's LocationManager.
    It returns true if either provider is enabled, and false otherwise.
     */
    private fun isLocationEnable(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /**
    This code is for updating the device's location by requesting location updates from the Fused Location Provider
    Client.
     */
    private fun updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest(), locationCallback,
            Looper.myLooper()
        )
    }

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {

            val location: Location? = p0.lastLocation

            if (location != null) {
                updateAddressUI(location)


            }

        }
    }

    fun updateAddressUI(location: Location) {

        val addressList: ArrayList<Address>
//https://stackoverflow.com/questions/74709477/get-address-from-current-location-kotlin
        val geocoder = Geocoder(this, Locale.getDefault())

        addressList = geocoder.getFromLocation(
            location.latitude,
            location.longitude,
            1
        ) as ArrayList<Address>

        val countryName = addressList[0].countryName
        Constants.COUNTRY_CODE = countryToCode(countryName)
        splashLoading()

    }

    // inspire by an open source on github :
    // https://github.com/ezatpanah/Local-Json-Into-Spinner
    private fun countryToCode(countryName: String): String {
        var country = ""
        var value = ""
        val json = loadJsonObjectFromAsset("countries.json")
        val refArray = json!!.getJSONArray("countries")
        for (i in 0 until refArray.length()) {
            country = refArray.getJSONObject(i).getString("name")
            value = refArray.getJSONObject(i).getString("value")
            if (countryName == country) {
                break
            } else {
                value = "us"
            }
        }
        return value
    }

    //Looking for a permission to access to the location
    private fun checkForLocationPermission(): Boolean {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
            return true
        return false
    }

    private fun loadJsonObjectFromAsset(assetName: String): JSONObject? {
        try {
            val json = loadStringFromAsset(assetName)
            return JSONObject(json)
        } catch (e: Exception) {
            Timber.tag("JsonUtils").e(e.toString())
        }
        return null
    }

    /**
    This code is loading a file from the assets folder of the app and returning it as a string.
    The function takes an assetName as a parameter which is the name of the file that needs to be loaded.
     */
    @Throws(Exception::class)
    private fun loadStringFromAsset(assetName: String): String {
        val `is`: InputStream = this.assets.open(assetName)
        val size: Int = `is`.available()
        val buffer = ByteArray(size)
        `is`.read(buffer)
        `is`.close()
        return String(buffer)
    }

    private fun askLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            locationRequestId
        )
    }

    /**
    The method checks if the request code is equal to a specific value (locationRequestId) and, if so,
    checks if the first element of the grantResults array is equal to PackageManager.PERMISSION_GRANTED.
    If both conditions are true, it calls the getLocation() method.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestId) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            }
        }

    }

    private fun locationEnabling() {
        googleApiClient = GoogleApiClient.Builder(this@SplashActivity)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {}
                override fun onConnectionSuspended(i: Int) {
                    googleApiClient!!.connect()
                }
            })
            .addOnConnectionFailedListener { connectionResult ->
                Timber.tag("Location error").d("Location error %s", connectionResult.errorCode)
            }.build()
        googleApiClient!!.connect()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest())
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> = LocationServices.SettingsApi
            .checkLocationSettings(googleApiClient!!, builder.build())
        result.setResultCallback {
            val status: Status = it.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    status.startResolutionForResult(this@SplashActivity, requestLocation)
                } catch (_: IntentSender.SendIntentException) {
                }
            }
        }
    }

    private fun locationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(3000)
            .setMaxUpdateDelayMillis(3000)
            .build()
    }

    //got help from "Using MotionLayout to Animate Android Apps" on Udacity -> ADVANCED ANDROID APPS WITH KOTLIN - PART 1
    private fun splashLoading() {
        binding.splashMotionLayout.transitionToState(R.id.end)
        binding.splashMotionLayout.addTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {}
            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {}
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }

            override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {}
        })

    }
}
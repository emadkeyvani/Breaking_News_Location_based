package com.keyvani.breaking_news.ui

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import com.keyvani.breaking_news.R
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.keyvani.breaking_news.ui.bookmarks.BookmarksFragment
import com.keyvani.breaking_news.ui.breakingnews.BreakingNewsFragment
import com.keyvani.breaking_news.ui.search.SearchNewsFragment
import com.keyvani.breaking_news.utils.Constants.COUNTRY_CODE
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.keyvani.breaking_news.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import java.io.InputStream
import java.util.*


@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // in the MainActivity , we want to handle bottom navigation manually.
    // because if we use normal code for bottom nav, each time when we click on each icon of fragment
    // that fragment recreated for use , we want to prevent this act

    private lateinit var binding: ActivityMainBinding
    private lateinit var breakingNewsFragment: BreakingNewsFragment
    private lateinit var searchNewsFragment: SearchNewsFragment
    private lateinit var bookmarksFragment: BookmarksFragment

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient
    private val locationRequestId = 100
    private var googleApiClient: GoogleApiClient? = null
    private val requestLocation = 199

    private val fragments: Array<Fragment>
        get() = arrayOf(
            breakingNewsFragment,
            searchNewsFragment,
            bookmarksFragment
        )

    private var selectedIndex = 0

    private val selectedFragment get() = fragments[selectedIndex]


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLocation()

        if (savedInstanceState == null) {
            breakingNewsFragment = BreakingNewsFragment()
            searchNewsFragment = SearchNewsFragment()
            bookmarksFragment = BookmarksFragment()

            supportFragmentManager.beginTransaction()
                .add(R.id.navContainer, breakingNewsFragment, TAG_BREAKING_NEWS_FRAGMENT)
                .add(R.id.navContainer, searchNewsFragment, TAG_SEARCH_NEWS_FRAGMENT)
                .add(R.id.navContainer, bookmarksFragment, TAG_BOOKMARKS_FRAGMENT)
                .commit()
        } else {
            breakingNewsFragment =
                supportFragmentManager.findFragmentByTag(TAG_BREAKING_NEWS_FRAGMENT) as BreakingNewsFragment
            searchNewsFragment =
                supportFragmentManager.findFragmentByTag(TAG_SEARCH_NEWS_FRAGMENT) as SearchNewsFragment
            bookmarksFragment =
                supportFragmentManager.findFragmentByTag(TAG_BOOKMARKS_FRAGMENT) as BookmarksFragment

            selectedIndex = savedInstanceState.getInt(KEY_SELECTED_INDEX, 0)
        }

        selectFragment(selectedFragment)

        binding.bottomNav.setOnNavigationItemSelectedListener { item ->

            val fragment = when (item.itemId) {
                R.id.nav_breaking -> breakingNewsFragment
                R.id.nav_search -> searchNewsFragment
                R.id.nav_bookmarks -> bookmarksFragment
                else -> throw IllegalArgumentException("Unexpected itemId")
            }

            if (selectedFragment === fragment) {
                fragment.onBottomNavigationFragmentReselected()
            } else {
                selectFragment(fragment)
            }

            true
        }
    }

    interface OnBottomNavigationFragmentReselectedListener {
        fun onBottomNavigationFragmentReselected()
    }


    private fun selectFragment(selectedFragment: Fragment) {
        var transaction = supportFragmentManager.beginTransaction()
        fragments.forEachIndexed { index, fragment ->
            if (selectedFragment == fragment) {
                transaction = transaction.attach(fragment)
                selectedIndex = index
            } else {
                transaction = transaction.detach(fragment)
            }
        }
        transaction.commit()

        title = when (selectedFragment) {
            is BreakingNewsFragment -> getString(R.string.title_breaking_news)
            is SearchNewsFragment -> getString(R.string.title_search_news)
            is BookmarksFragment -> getString(R.string.title_bookmarks)
            else -> ""
        }
    }

    override fun onBackPressed() {
        if (selectedIndex != 0) {
            binding.bottomNav.selectedItemId = R.id.nav_breaking
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_INDEX, selectedIndex)
    }

    private fun getLocation() {

        if (checkForLocationPermission()) {
            if (isLocationEnable()) {
                updateLocation()
            } else {
                enableLoc()
                updateLocation()
            }
        } else {
            askLocationPermission()
        }
    }

    private fun isLocationEnable(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mFusedLocationProviderClient.requestLocationUpdates(
            locationRequest(), mLocationCallback,
            Looper.myLooper()
        )
    }

    private var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {

            val location: Location? = p0.lastLocation

            if (location != null) {
                updateAddressUI(location)
            }

        }
    }

    fun updateAddressUI(location: Location) {

        val addressList: ArrayList<Address>

        val geocoder = Geocoder(applicationContext, Locale.getDefault())

        addressList = geocoder.getFromLocation(
            location.latitude,
            location.longitude,
            1
        ) as ArrayList<Address>

        val countryName = addressList[0].countryName
        COUNTRY_CODE = countryToCode(countryName)

    }

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
            Log.e("JsonUtils", e.toString())
        }
        return null
    }

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

    private fun enableLoc() {
        googleApiClient = GoogleApiClient.Builder(this@MainActivity)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {}
                override fun onConnectionSuspended(i: Int) {
                    googleApiClient!!.connect()
                }
            })
            .addOnConnectionFailedListener { connectionResult ->
                Log.d("Location error", "Location error " + connectionResult.errorCode)
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
                    status.startResolutionForResult(this@MainActivity, requestLocation)
                } catch (_: IntentSender.SendIntentException) {
                }
            }
        }
    }

    private fun locationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(1000)
            .setMaxUpdateDelayMillis(1000)
            .build()
    }


}

private const val TAG_BREAKING_NEWS_FRAGMENT = "TAG_BREAKING_NEWS_FRAGMENT"
private const val TAG_SEARCH_NEWS_FRAGMENT = "TAG_SEARCH_NEWS_FRAGMENT"
private const val TAG_BOOKMARKS_FRAGMENT = "TAG_BOOKMARKS_FRAGMENT"
private const val KEY_SELECTED_INDEX = "KEY_SELECTED_INDEX"

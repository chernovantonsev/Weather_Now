package ru.antonc.weather.ui.main

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ru.antonc.weather.R
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MainActivity : AppCompatActivity(), HasAndroidInjector {

    companion object {
        private const val REQUEST_LOCATION = 300

        private const val INTERVAL_TO_UPDATE_LOCATION = 10L
        private const val DISTANCE_TO_UPDATE_LOCATION = 2500F
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainViewModel

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationManager: LocationManager? = null
    private var locationListener: android.location.LocationListener? = null


    private val locationRequest: LocationRequest = LocationRequest().apply {
        priority = PRIORITY_LOW_POWER
        interval = TimeUnit.MINUTES.toMillis(INTERVAL_TO_UPDATE_LOCATION)
        smallestDisplacement = DISTANCE_TO_UPDATE_LOCATION
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        if (checkLocationPermission())
            initLocationListener()
        else requestPermissionLocation()

        viewModel.isNeedUpdateLocationSetting.observe(this@MainActivity) { isLocationUpdateOn ->
            if (isLocationUpdateOn)
                startLocationUpdates()
            else stopLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    private fun initLocationListener() {
        if (isGooglePlayServicesAvailable())
            runLocationListener()
        else runLocationListenerWithoutPLayServices()
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    private fun runLocationListener() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                viewModel.saveLastLocation(locationResult.lastLocation)
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
            location?.let {
                viewModel.saveLastLocation(location)
            }
        }
    }

    private fun runLocationListenerWithoutPLayServices() {
        locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationListener =
            android.location.LocationListener { location ->
                viewModel.saveLastLocation(location)
            }
    }

    private fun requestPermissionLocation() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION -> if (grantResults.isNotEmpty()) {
                val permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (permissionGranted) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.permission_granted),
                        Toast.LENGTH_LONG
                    ).show()
                    if (checkLocationPermission())
                        initLocationListener()
                } else {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.permission_not_granted),
                        Toast.LENGTH_LONG
                    ).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            showDialogIsNeedLocation(getString(R.string.to_continue_need_permission_location),
                                DialogInterface.OnClickListener { _, _ ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(
                                            arrayOf(Manifest.permission.CAMERA),
                                            REQUEST_LOCATION
                                        )
                                    }
                                })
                            return
                        }
                    }
                }
            }
        }
    }

    private fun showDialogIsNeedLocation(
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        androidx.appcompat.app.AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setPositiveButton(getString(R.string.good), okListener)
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> onBackPressed() }
            .create()
            .show()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun startLocationUpdates() {
        if (checkLocationPermission()) {
            locationCallback?.let { locationCallback ->
                fusedLocationClient?.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }

            locationListener?.let { locationListener ->
                if (locationManager?.allProviders?.contains(LocationManager.NETWORK_PROVIDER) == true)
                    locationManager?.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        TimeUnit.MINUTES.toMillis(INTERVAL_TO_UPDATE_LOCATION),
                        DISTANCE_TO_UPDATE_LOCATION,
                        locationListener
                    );

                if (locationManager?.allProviders?.contains(LocationManager.GPS_PROVIDER) == true)
                    locationManager?.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, TimeUnit.MINUTES.toMillis(
                            INTERVAL_TO_UPDATE_LOCATION
                        ),
                        DISTANCE_TO_UPDATE_LOCATION, locationListener
                    )
            }
        }
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { locationCallback ->
            fusedLocationClient?.removeLocationUpdates(locationCallback)
        }
        locationListener?.let { locationListener ->
            locationManager?.removeUpdates(locationListener)
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(this)
        return status == ConnectionResult.SUCCESS
    }

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}
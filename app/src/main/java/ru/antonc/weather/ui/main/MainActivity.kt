package ru.antonc.weather.ui.main

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ru.antonc.weather.R
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasAndroidInjector {

    companion object {
        private const val REQUEST_LOCATION = 300
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MainViewModel

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLocation()
            return
        } else {
            runLocationListener()
        }
    }

    @RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
    private fun runLocationListener() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    viewModel.saveLastLocation(location)
                }
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
                    runLocationListener()
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

    override fun androidInjector(): AndroidInjector<Any> = androidInjector
}
package com.example.incivismoadrianpeiro.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import  android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.firebase.auth.FirebaseUser
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executors

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application = application

    private val currentAddress = MutableLiveData<String>()
    val checkPermission = MutableLiveData<String>()
    private val buttonText = MutableLiveData<String>()
    private val progressBar = MutableLiveData<Boolean>()

    private var mTrackingLocation: Boolean = false
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                fetchAddress(location)
            }
        }
    }

    fun setFusedLocationClient(client: FusedLocationProviderClient) {
        mFusedLocationClient = client
    }

    fun getCurrentAddress(): LiveData<String> = currentAddress

    fun getButtonText(): LiveData<String> = buttonText

    fun getProgressBar(): LiveData<Boolean> = progressBar

    fun getCheckPermission(): LiveData<String> = checkPermission

    private fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun switchTrackingLocation() {
        if (!mTrackingLocation) {
            startTrackingLocation(needsChecking = true)
        } else {
            stopTrackingLocation()
        }
    }


    @SuppressLint("MissingPermission")
    fun startTrackingLocation(needsChecking: Boolean) {
        if (needsChecking) {
            checkPermission.postValue("listo")
        } else {
            mFusedLocationClient?.requestLocationUpdates(getLocationRequest(), mLocationCallback, null)
            currentAddress.postValue("Carregant...")
            progressBar.postValue(true)
            mTrackingLocation = true
            buttonText.value = "apaga el seguimiento"
        }
    }

    private fun stopTrackingLocation() {
        if (mTrackingLocation) {
            mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
            mTrackingLocation = false
            progressBar.postValue(false)
            buttonText.value = "Comineza a seguir"
        }
    }

    private fun fetchAddress(location: Location) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        val geocoder = Geocoder(app.applicationContext, Locale.getDefault())

        executor.execute {
            var resultMessage = ""

            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses.isNullOrEmpty()) {
                    resultMessage = "No se ha encontrado"
                    Log.e("INCIVISME", resultMessage)
                } else {
                    val address = addresses[0]
                    val addressParts = (0..address.maxAddressLineIndex).map { address.getAddressLine(it) }
                    resultMessage = addressParts.joinToString("\n")
                    val finalResultMessage = resultMessage
                    handler.post {
                        if (mTrackingLocation) {
                            currentAddress.postValue(
                                "Direcció: $finalResultMessage \n Hora: ${System.currentTimeMillis()}"
                            )
                        }
                    }
                }
            } catch (ioException: IOException) {
                resultMessage = "Servei no disponible"
                Log.e("INCIVISME", resultMessage, ioException)
            } catch (illegalArgumentException: IllegalArgumentException) {
                resultMessage = "Coordenades no valides"
                Log.e(
                    "INCIVISME",
                    "$resultMessage. Latitude = ${location.latitude}, Longitude = ${location.longitude}",
                    illegalArgumentException
                )
            }
        }
    }

    var user: MutableLiveData<FirebaseUser> = MutableLiveData()
    fun getUser(): LiveData<FirebaseUser>{
        return user
    }

    fun setUser(passedUser: FirebaseUser){
        user.postValue(passedUser)
    }



    




    fun get(java: Class<HomeViewModel>) {

    }
}

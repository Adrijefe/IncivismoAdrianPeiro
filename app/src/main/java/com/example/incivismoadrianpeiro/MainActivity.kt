package com.example.incivismoadrianpeiro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.incivismoadrianpeiro.databinding.ActivityMainBinding
import com.example.incivismoadrianpeiro.ui.home.HomeViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import java.util.Arrays
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
        ).build()

        val navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.navView, navController)

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        homeViewModel.setFusedLocationClient(mFusedLocationClient)

        homeViewModel.checkPermission.observe(this) {
            checkPermission()
        }

        locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val fineLocationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            when {
                fineLocationGranted -> homeViewModel.startTrackingLocation(false)
                coarseLocationGranted -> homeViewModel.startTrackingLocation(false)
                else -> Toast.makeText(this, "No coinciden los permisos", Toast.LENGTH_SHORT).show()
            }
        }

        signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    homeViewModel.setUser(user)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val auth = FirebaseAuth.getInstance()
        Log.e("XXXX", auth.currentUser.toString())
        if (auth.currentUser == null) {
            val signInIntent =
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(
                        Arrays.asList(
                            EmailBuilder().build(),
                            GoogleBuilder().build()
                        )
                    )
                    .build()
            signInLauncher.launch(signInIntent)
        } else {
            homeViewModel.setUser(auth.currentUser!!)
        }
    }

    private fun checkPermission() {
        Log.d("PERMISSIONS", "Check permissions")
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("PERMISSIONS", "Request permissions")
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            homeViewModel.startTrackingLocation(false)
        }
    }
}

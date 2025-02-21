package com.example.incivismoadrianpeiro;

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.example.incivismoadrianpeiro.R
import com.example.incivismoadrianpeiro.databinding.ActivityMainBinding
import com.example.incivismoadrianpeiro.ui.home.HomeViewModel
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import java.util.Arrays

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>
    private lateinit var viewModel: HomeViewModel
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
        )
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        viewModel.setFusedLocationClient(fusedLocationClient)

        viewModel.getCheckPermission().observe(this) {
            checkPermission()
        }

        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val fineLocationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseLocationGranted = result[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineLocationGranted || coarseLocationGranted) {
                viewModel.startTrackingLocation(false)
            } else {
                Toast.makeText(this, "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show()
            }
        }

        signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract()
        ) { result: FirebaseAuthUIAuthenticationResult ->
            if (result.resultCode == RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                viewModel.setUser(user!!)
            } else {
                Toast.makeText(this, "Inicio de sesión fallido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermission() {
        Log.d("PERMISSIONS", "Revisando permisos")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.startTrackingLocation(false)
        }
    }

    override fun onStart() {
        super.onStart()

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            val signInIntent = AuthUI.getInstance()
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
            viewModel.setUser(auth.currentUser!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}


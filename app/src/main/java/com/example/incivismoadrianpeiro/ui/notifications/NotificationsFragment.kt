package com.example.incivismoadrianpeiro.ui.notifications

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.incivismoadrianpeiro.databinding.FragmentNotificationsBinding
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null


    private val binding get() = _binding!!
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 1

    companion object {
        const val REQUEST_TAKE_PHOTO = 1
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.MapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.MapView.setMultiTouchControls(true)
        val mapController = binding.MapView.controller
        mapController.setZoom(14.5)
        val startPoint = GeoPoint(37.4219983, -122.084)
        mapController.setCenter(startPoint)

        val myLocationNewOverlay =
            MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), binding.MapView)
        myLocationNewOverlay.enableMyLocation()
        binding.MapView.overlays.add(myLocationNewOverlay)


        val compassOverlay = CompassOverlay(
            requireContext(),
            InternalCompassOrientationProvider(requireContext()),
            binding.MapView
        )
        compassOverlay.enableCompass()
        binding.MapView.overlays.add(compassOverlay)

        requestPermissionsIfNecessary(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))




        return root
    }


    override fun onResume() {
        super.onResume()
        binding.MapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.MapView.onPause()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val permissionsToRequest = ArrayList<String>()
        for (i in grantResults.indices) {
            permissionsToRequest.add(permissions[i])
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray<String>(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun requestPermissionsIfNecessary(permissions: Array<String>) {
        val permissionsToRequest = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(requireActivity(), permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                permissionsToRequest.toTypedArray<String>(),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

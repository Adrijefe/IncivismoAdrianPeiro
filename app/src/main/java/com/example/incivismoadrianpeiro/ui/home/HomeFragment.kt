package com.example.incivismoadrianpeiro.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.incivismoadrianpeiro.R
import com.example.incivismoadrianpeiro.databinding.FragmentHomeBinding
import com.example.incivismoadrianpeiro.ui.Incidencia
import com.example.incivismoadrianpeiro.ui.notifications.NotificationsFragment.Companion.REQUEST_TAKE_PHOTO

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var authUser: FirebaseUser? = null
    private var mCurrentPhotoPath: String? = null
    private var photoURI: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        sharedViewModel.getCurrentAddress().observe(viewLifecycleOwner) { address ->
            binding.txtDireccio.text = Editable.Factory.getInstance().newEditable(
                String.format(
                    "Direcció: %1\$s \n Hora: %2\$tr",
                    address.toString(), System.currentTimeMillis()
                )
            )
        }

        sharedViewModel.getCurrentLatLng().observe(viewLifecycleOwner) { latlng ->
            binding.txtLatitud.setText(java.lang.String.valueOf(latlng.latitude))
            binding.txtLongitud.setText(java.lang.String.valueOf(latlng.longitude))
        }

        sharedViewModel.getProgressBar().observe(
            viewLifecycleOwner
        ) { visible: Boolean ->
            if (visible) binding.loading.visibility = ProgressBar.VISIBLE
            else binding.loading.visibility = ProgressBar.INVISIBLE
        }
        sharedViewModel.getUser().observe(viewLifecycleOwner) { user ->
            authUser = user
        }

        sharedViewModel.switchTrackingLocation()

        binding.buttonNotificar.setOnClickListener {
            val incidencia = Incidencia().apply {
                direccio = binding.txtDireccio.text.toString()
                latitud = binding.txtLatitud.text.toString()
                longitud = binding.txtLongitud.text.toString()
                descripcio = binding.txtDescripcio.text.toString()
            }

            Log.d("HomeFragment", "Dirección: ${incidencia.direccio}")
            Log.d("HomeFragment", "Latitud: ${incidencia.latitud}")
            Log.d("HomeFragment", "Longitud: ${incidencia.longitud}")
            Log.d("HomeFragment", "Descripcio: ${incidencia.descripcio}")

            if (incidencia.direccio!!.isNotEmpty() && incidencia.latitud!!.isNotEmpty() && incidencia.longitud!!.isNotEmpty() && incidencia.descripcio!!.isNotEmpty()) {
                Log.d("HomeFragment", "ahi te va")

                val database = FirebaseDatabase.getInstance("https://incivismoadrianpeiro-default-rtdb.europe-west1.firebasedatabase.app/")
                val reference = database.reference

                val users = reference.child("users")
                val uid = users.child(authUser?.uid ?: "")
                val incidencies = uid.child("incidencies")
                val referencePush = incidencies.push()
                referencePush.setValue(incidencia).addOnCompleteListener { task ->

                }
            } else {
                Log.e(
                    "HomeFragment",
                    "Algunos campos están vacíos. No se puede enviar la incidencia."
                )
            }
        }

        binding.buttonFoto.setOnClickListener {
            Log.d("XXX", "foto")
            dispatchTakePictureIntent()

        }


        return root
    }
    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        Log.d("XXX", context?.packageManager?.let { takePictureIntent.resolveActivity(it) }.toString())
        if (context?.packageManager?.let { takePictureIntent.resolveActivity(it) } != null) {
            Log.d("XXX", "Caba es marica")
            var photoFile: File? = null
            try {
                photoFile = createImagenFile()
            } catch (ex: IOException) {

                ex.printStackTrace()
            }

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.android.fileprovider",
                    photoFile
                )
                Log.d("XXX","CABA ES GAY")
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                Glide.with(this).load(photoURI).into(binding!!.foto)
            } else {
                Toast.makeText(context, "La foto no esta hecha", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun createImagenFile(): File {
        val timestamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        Log.d("XXX","CABA ES GAY2")
        val imageFileName = "JPEG_${timestamp}_"
        val storageDir: File? = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image: File = File.createTempFile(
            imageFileName,
            "jpg",
            storageDir

        )
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

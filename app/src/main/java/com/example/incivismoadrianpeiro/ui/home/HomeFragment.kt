package com.example.incivismoadrianpeiro.ui.home

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.incivismoadrianpeiro.databinding.FragmentHomeBinding
import com.example.incivismoadrianpeiro.ui.Incidencia
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var authUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this).get(HomeViewModel::class.java)

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
            Log.d("HomeFragment", "Problema: ${incidencia.descripcio}")

            if (incidencia.direccio!!.isNotEmpty() && incidencia.latitud!!.isNotEmpty() && incidencia.longitud!!.isNotEmpty() && incidencia.descripcio!!.isNotEmpty()) {
                val database = FirebaseDatabase.getInstance("https://incivismoadrianpeiro-default-rtdb.europe-west1.firebasedatabase.app")
                val reference = database.reference
                val users = reference.child("users")
                val uid = users.child(authUser?.uid ?: "")
                val incidencies = uid.child("incidencies")

                val referencePush = incidencies.push()
                referencePush.setValue(incidencia).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("HomeFragment", "Incidencia enviada correctamente")
                    } else {
                        Log.e("HomeFragment", "Error al enviar la incidencia", task.exception)
                    }
                }
            } else {
                Log.e(
                    "HomeFragment",
                    "Algunos campos están vacíos. No se puede enviar la incidencia."
                )
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
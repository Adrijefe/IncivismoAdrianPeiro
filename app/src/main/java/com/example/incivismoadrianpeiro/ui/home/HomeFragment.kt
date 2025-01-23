package com.example.incivismoadrianpeiro.ui.home

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.incivismoadrianpeiro.databinding.FragmentHomeBinding
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val sharedViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        sharedViewModel.getCurrentAddress().observe(viewLifecycleOwner) { address ->
            binding.localizacion.text = String.format(
                "Dirección: %1\$s \n Hora: %2\$tr",
                address, System.currentTimeMillis()
            )
        }

        sharedViewModel.getButtonText().observe(viewLifecycleOwner) { text ->
            binding.PushButtonHome.text = text
        }

        sharedViewModel.getProgressBar().observe(viewLifecycleOwner) { visible ->
            binding.textHome.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        }

        binding.PushButtonHome.setOnClickListener {
            Log.d("DEBUG", "Clicked Get Location")
            sharedViewModel.switchTrackingLocation()
        }



        return root



        }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
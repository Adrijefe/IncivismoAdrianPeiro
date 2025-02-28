package com.example.incivismoadrianpeiro.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.incivismoadrianpeiro.databinding.FragmentDashboardBinding
import com.example.incivismoadrianpeiro.databinding.RvIncidenciesItemBinding
import com.example.incivismoadrianpeiro.ui.Incidencia
import com.example.incivismoadrianpeiro.ui.home.HomeViewModel
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var authUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        sharedViewModel.user.observe(viewLifecycleOwner) { user ->
            authUser = user

            if (user != null) {
                val base = FirebaseDatabase.getInstance("https://incivismoadrianpeiro-default-rtdb.europe-west1.firebasedatabase.app").reference

                val users = base.child("users")
                val uid = users.child(user.uid)
                val incidencies = uid.child("com.example.incivismoadrianpeiro.getIncidencies")

                val options = FirebaseRecyclerOptions.Builder<Incidencia>()
                    .setQuery(incidencies, Incidencia::class.java)
                    .setLifecycleOwner(this)
                    .build()

                val adapter = IncidenciaAdapter(options)

                binding.rvIncidencies.adapter = adapter
                binding.rvIncidencies.layoutManager = LinearLayoutManager(requireContext())
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class IncidenciaAdapter(options: FirebaseRecyclerOptions<Incidencia>) :
        FirebaseRecyclerAdapter<Incidencia, IncidenciaAdapter.IncidenciaViewHolder>(options) {

        override fun onBindViewHolder(
            holder: IncidenciaViewHolder,
            position: Int,
            model: Incidencia
        ) {
            Log.d("NO va", "" + model.descripcio);
            holder.binding.txtDescripcio.text = model.descripcio
            holder.binding.txtAdreca.text = model.direccio
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
            val binding = RvIncidenciesItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
            return IncidenciaViewHolder(binding)
        }

        inner class IncidenciaViewHolder(val binding: RvIncidenciesItemBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}


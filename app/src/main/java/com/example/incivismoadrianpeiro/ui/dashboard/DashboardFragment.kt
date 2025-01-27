package com.example.incivismoadrianpeiro.ui.dashboard

import android.os.Bundle
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
import com.firebase.ui.auth.data.model.User
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
class DashboardFragment : Fragment() {
    private var binding: FragmentDashboardBinding? = null
    private var authUser: FirebaseUser ? = null
    private var adapter: IncidenciaAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ViewModelProvider(this)[DashboardViewModel::class.java]

        val sharedViewModel: HomeViewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        sharedViewModel.getUser ().observe(viewLifecycleOwner) { user ->
            authUser = user
            if (user != null) {
                val base = FirebaseDatabase.getInstance("https://incivismoadrianpeiro-default-rtdb.europe-west1.firebasedatabase.app").reference
                val users = base.child("users")
                val userReference = users.child(user.uid)
                val incidencies = userReference.child("incidencies")

                val options = FirebaseRecyclerOptions.Builder<Incidencia>()
                    .setQuery(incidencies, Incidencia::class.java)
                    .setLifecycleOwner(viewLifecycleOwner)
                    .build()

                adapter = IncidenciaAdapter(options)

                binding?.rvIncidencies?.adapter = adapter
                binding?.rvIncidencies?.layoutManager = LinearLayoutManager(requireContext())
                adapter?.startListening()
            }
        }
        return binding?.root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    internal inner class IncidenciaAdapter(options: FirebaseRecyclerOptions<Incidencia?>) :
        FirebaseRecyclerAdapter<Incidencia, IncidenciaAdapter.IncidenciaViewholder>(options) {
        override fun onBindViewHolder(
            holder: IncidenciaViewholder, position: Int, model: Incidencia
        ) {
            holder.binding.txtDescripcio.text = model.descripcio
            holder.binding.txtAdreca.text = model.direccio
        }

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): IncidenciaViewholder {
            return IncidenciaViewholder(
                RvIncidenciesItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                )
            )
        }

        internal inner class IncidenciaViewholder(var binding: RvIncidenciesItemBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
package cl.friendlypos.mypos.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.databinding.FragmentHomeBinding
import cl.friendlypos.mypos.model.DashboardItem
import cl.friendlypos.mypos.ui.adapters.DashboardAdapter

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDashboard()
    }

    private fun setupDashboard() {
        // Define los elementos del dashboard
        val dashboardItems = listOf(
            DashboardItem(1, "Nueva Venta", R.drawable.ic_new_sale, 
                          ContextCompat.getColor(requireContext(), R.color.tile_sale)),
            DashboardItem(2, "Inventario", R.drawable.ic_inventory,
                          ContextCompat.getColor(requireContext(), R.color.tile_inventory)),
            DashboardItem(3, "Clientes", R.drawable.ic_customers,
                          ContextCompat.getColor(requireContext(), R.color.tile_customers)),
            DashboardItem(4, "Reportes", R.drawable.ic_reports,
                          ContextCompat.getColor(requireContext(), R.color.tile_reports)),
            // Añade más elementos según necesites
        )

        val adapter = DashboardAdapter(dashboardItems) { item ->
            when (item.id) {
                1 -> navigateToNewSale()
                2 -> navigateToInventory()
                3 -> navigateToCustomers()
                4 -> navigateToReports()
                // Maneja más casos según necesites
            }
        }

        binding.recyclerDashboard.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter = adapter
        }
    }

    private fun navigateToNewSale() {
        // Implementar navegación
    }

    private fun navigateToInventory() {
        // Implementar navegación
    }

    private fun navigateToCustomers() {
        // Implementar navegación
    }

    private fun navigateToReports() {
        // Implementar navegación
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
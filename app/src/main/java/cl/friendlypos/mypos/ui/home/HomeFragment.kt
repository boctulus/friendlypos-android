package cl.friendlypos.mypos.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import cl.friendlypos.mypos.CashFundActivity
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.databinding.FragmentHomeBinding
import cl.friendlypos.mypos.model.DashboardItem
import cl.friendlypos.mypos.ui.dashboard.DashboardAdapter

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
        val dashboardItems = listOf(
            DashboardItem(1, "Nueva Venta", R.drawable.ic_new_sale, 
                          ContextCompat.getColor(requireContext(), R.color.tile_sale)),
            DashboardItem(2, "Inventario", R.drawable.ic_inventory,
                          ContextCompat.getColor(requireContext(), R.color.tile_inventory)),
            DashboardItem(3, "Clientes", R.drawable.ic_customers,
                          ContextCompat.getColor(requireContext(), R.color.tile_customers)),
            DashboardItem(4, "Reportes", R.drawable.ic_reports,
                          ContextCompat.getColor(requireContext(), R.color.tile_reports))
        )

        val adapter = DashboardAdapter(dashboardItems) { item ->
            when (item.id) {
                1 -> navigateToNewSale()
                2 -> navigateToInventory()
                3 -> navigateToCustomers()
                4 -> navigateToReports()
            }
        }

        binding.recyclerDashboard.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            this.adapter = adapter
        }
    }

    private fun navigateToNewSale() {
        view?.findNavController()?.navigate(R.id.action_home_to_sales_calc)
    }

    private fun navigateToInventory() {
        view?.findNavController()?.navigate(R.id.action_home_to_inventory)
    }

    private fun navigateToCustomers() {
        view?.findNavController()?.navigate(R.id.action_home_to_customers)
    }

    private fun navigateToReports() {
        view?.findNavController()?.navigate(R.id.action_home_to_reports)
    }

    private fun navigateToCashFund() {
        startActivity(Intent(requireContext(), CashFundActivity::class.java))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}